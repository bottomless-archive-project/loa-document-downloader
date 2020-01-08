package com.github.loa.downloader.service.file;

import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.downloader.configuration.DownloaderConfigurationProperties;
import com.github.loa.parser.service.DocumentDataParser;
import com.github.loa.stage.service.StageLocationFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.InputStream;

/**
 * Validates a document after it's downloaded to the staging area.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentFileValidator {

    // Anything under 1024 bytes is likely to be some bit garbage.
    private static final int MINIMUM_FILE_LENGTH = 1024;

    private final DocumentDataParser documentDataParser;
    private final StageLocationFactory stageLocationFactory;
    private final DownloaderConfigurationProperties downloaderConfigurationProperties;

    /**
     * Evaluates a document's validity based on the document's properties in the staging area.
     *
     * @param documentId   the id of the document to validate
     * @param documentType the type of the document to validate
     */
    public Mono<Boolean> isValidDocument(final String documentId, final DocumentType documentType) {
        return stageLocationFactory.getLocation(documentId, documentType)
                .map(stageFileLocation -> {
                    if (!isValidFileSize(stageFileLocation.size())) {
                        return false;
                    }

                    try (final InputStream inputStream = stageFileLocation.openStream()) {
                        documentDataParser.parseDocumentMetadata(documentId, documentType, inputStream);
                    } catch (final Exception e) {
                        log.info("Non-parsable document: {}!", documentId);

                        return false;
                    }

                    return true;
                });
    }

    private boolean isValidFileSize(final long stageFileSize) {
        return stageFileSize > MINIMUM_FILE_LENGTH
                && stageFileSize < downloaderConfigurationProperties.getMaximumArchiveSize();
    }
}
