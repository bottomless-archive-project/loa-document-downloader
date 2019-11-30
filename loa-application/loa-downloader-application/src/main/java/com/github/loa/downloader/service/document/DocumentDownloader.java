package com.github.loa.downloader.service.document;

import com.github.loa.document.service.DocumentTypeCalculator;
import com.github.loa.document.service.domain.DocumentType;
import com.github.loa.downloader.service.file.DocumentFileManipulator;
import com.github.loa.downloader.service.file.DocumentFileValidator;
import com.github.loa.downloader.service.file.FileCollector;
import com.github.loa.source.domain.DocumentSourceItem;
import com.github.loa.stage.service.StageLocationFactory;
import com.github.loa.stage.service.domain.StageLocation;
import com.github.loa.vault.client.service.domain.ArchivingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.UUID;

/**
 * This service is responsible for downloading documents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentDownloader {

    private final StageLocationFactory stageLocationFactory;
    private final DocumentFileValidator documentFileValidator;
    private final DocumentFileManipulator documentFileManipulator;
    private final FileCollector fileCollector;
    private final DocumentTypeCalculator documentTypeCalculator;

    public Mono<ArchivingContext> downloadDocument(final DocumentSourceItem documentSourceItem) {
        final URL documentLocation = documentSourceItem.getDocumentLocation();
        final DocumentType documentType = documentTypeCalculator.calculate(documentLocation)
                .orElseThrow(() -> new RuntimeException("Unable to find valid document type for document: "
                        + documentLocation));

        log.debug("Starting to download document {}.", documentLocation);

        return Mono.just(UUID.randomUUID().toString())
                .flatMap(documentId -> stageLocationFactory.getLocation(documentId, documentType)
                        .flatMap(stageFileLocation -> acquireFile(documentLocation, stageFileLocation))
                        .flatMap(documentFileLocation -> documentFileValidator.isValidDocument(documentId, documentType)
                                .filter(validationResult -> !validationResult)
                                .thenReturn(documentFileLocation)
                                .doOnNext(StageLocation::cleanup)
                        )
                        .filter(StageLocation::exists)
                        .flatMap(stageLocation -> Mono.just(
                                ArchivingContext.builder()
                                        .location(documentLocation.toString())
                                        .source(documentSourceItem.getSourceName())
                                        .type(documentType)
                                        .contents(stageLocation.getPath())
                                        .build()
                                )
                        )
                        .onErrorResume(error -> {
                            log.debug("Error downloading a document: {}!", error.getMessage());

                            return Mono.empty();
                        })
                );
    }

    private Mono<StageLocation> acquireFile(final URL documentLocation, final StageLocation stageLocation) {
        return fileCollector.acquireFile(documentLocation, stageLocation.getPath())
                .thenReturn(stageLocation);
    }
}
