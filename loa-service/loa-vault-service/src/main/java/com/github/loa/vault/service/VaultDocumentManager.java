package com.github.loa.vault.service;

import com.github.loa.checksum.service.ChecksumProvider;
import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.compression.service.provider.CompressionServiceProvider;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.domain.DocumentStatus;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.document.service.entity.factory.domain.DocumentCreationContext;
import com.github.loa.stage.service.StageLocationFactory;
import com.github.loa.vault.configuration.VaultConfigurationProperties;
import com.github.loa.vault.domain.exception.VaultAccessException;
import com.github.loa.vault.service.domain.DocumentArchivingContext;
import com.github.loa.vault.service.location.VaultLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Provide access to the content of the documents in the vault.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VaultDocumentManager {

    private final ResourceLoader resourceLoader;
    private final VaultLocationFactory vaultLocationFactory;
    private final CompressionServiceProvider compressionServiceProvider;
    private final CompressionConfigurationProperties compressionConfigurationProperties;
    private final VaultConfigurationProperties vaultConfigurationProperties;
    private final DocumentEntityFactory documentEntityFactory;
    private final ChecksumProvider checksumProvider;
    private final StageLocationFactory stageLocationFactory;

    /**
     * Archive the content of an input stream as the content of the provided document in the vault.
     */
    public Mono<DocumentEntity> archiveDocument(final DocumentArchivingContext documentArchivingContext) {
        final String documentId = UUID.randomUUID().toString();

        log.info("Archiving document with id: {}.", documentId);

        return stageLocationFactory.getLocation(documentId, documentArchivingContext.getType())
                .map(stageLocation -> {
                    try (final OutputStream stageFileOutputStream =
                                 new FileOutputStream(stageLocation.getPath().toFile())) {
                        documentArchivingContext.getContent().transferTo(stageFileOutputStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return stageLocation;
                })
                .flatMap(stageLocation -> checksumProvider.checksum(documentId, stageLocation.getPath())
                        .filterWhen(checksum -> isDocumentMissing(checksum, documentArchivingContext))
                        .flatMap(checksum -> documentEntityFactory.newDocumentEntity(
                                DocumentCreationContext.builder()
                                        .id(documentId)
                                        .type(documentArchivingContext.getType())
                                        .status(DocumentStatus.DOWNLOADED)
                                        .source(documentArchivingContext.getSource())
                                        .versionNumber(vaultConfigurationProperties.getVersionNumber())
                                        .compression(compressionConfigurationProperties.getAlgorithm())
                                        .checksum(checksum)
                                        .fileSize(documentArchivingContext.getContentLength())
                                        .build()
                                )
                        )
                        .flatMap(documentEntity -> Mono.fromSupplier(
                                () -> saveDocument(documentEntity, stageLocation.getPath())))
                        .retry()
                );
    }

    public Mono<Boolean> isDocumentMissing(final String checksum,
            final DocumentArchivingContext documentArchivingContext) {
        return documentEntityFactory.isDocumentMissing(checksum, documentArchivingContext.getContentLength(),
                documentArchivingContext.getType())
                .doOnNext(missing -> {
                    if (!missing) {
                        log.info("Document with checksum {} is a duplicate.", checksum);
                    }
                });
    }

    public DocumentEntity saveDocument(final DocumentEntity documentEntity, final Path documentContents) {
        try (final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity);
             final InputStream documentInputStream = new FileInputStream(documentContents.toFile())) {
            saveDocumentContents(documentEntity, documentInputStream, vaultLocation);
        } catch (IOException e) {
            throw new VaultAccessException("Unable to move document with id " + documentEntity.getId()
                    + " to the vault!", e);
        }

        return documentEntity;
    }

    public void saveDocumentContents(final DocumentEntity documentEntity, final InputStream documentContents,
            final VaultLocation vaultLocation) throws IOException {
        if (!documentEntity.isCompressed()) {
            try (final OutputStream outputStream = vaultLocation.destination()) {
                IOUtils.copy(documentContents, outputStream);
            }
        } else {
            try (final OutputStream outputStream = compressionServiceProvider
                    .getCompressionService(documentEntity.getCompression()).compress(vaultLocation.destination())) {
                IOUtils.copy(documentContents, outputStream);
            }
        }
    }

    /**
     * Return the content of a document as an {@link InputStream}.
     *
     * @param documentEntity the document to return the content for
     * @return the content of the document
     */
    public Resource readDocument(final DocumentEntity documentEntity) {
        final VaultLocation vaultLocation = vaultLocationFactory.getLocation(documentEntity);

        // The non-compressed entries will be served via a zero-copy response
        // See: https://developer.ibm.com/articles/j-zerocopy/
        if (documentEntity.isCompressed()) {
            final InputStream decompressedInputStream = compressionServiceProvider
                    .getCompressionService(documentEntity.getCompression()).decompress(vaultLocation.content());

            return new InputStreamResource(decompressedInputStream);
        } else {
            return resourceLoader.getResource("file:/" + vaultLocation.file().getPath());
        }
    }

    /**
     * Remove the content of a document from the vault.
     *
     * @param documentEntity the document to remove
     * @return the document that was removed
     */
    public Mono<DocumentEntity> removeDocument(final DocumentEntity documentEntity) {
        return Mono.just(documentEntity)
                .map(vaultLocationFactory::getLocation)
                .doOnNext(VaultLocation::clear)
                .thenReturn(documentEntity);
    }
}
