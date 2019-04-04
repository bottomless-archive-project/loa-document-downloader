package com.github.loa.downloader.command.service;

import com.github.loa.downloader.download.service.document.DocumentDownloader;
import com.github.loa.source.service.DocumentSourceProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * This service is the hearth of the downloader application. It's responsible for the processing
 * (loading and downloading) of document locations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentSourceProcessor {

    private final DocumentDownloader documentDownloader;
    private final ExecutorService downloaderExecutor;
    private final Semaphore downloaderSemaphore;

    /**
     * Handles the processing of a document source.
     *
     * @param documentSource the access to the source as an URL stream
     */
    public void processDocumentSource(final Stream<URL> documentSource) {
        log.info("Starting to process a new document source.");

        documentSource
                .sequential()
                .filter(this::shouldDownload)
                .forEach(this::processLocation);

        shutdownGracefully();
    }

    private boolean shouldDownload(final URL documentLocation) {
        //Using getPath() to be able to crawl urls like: /example/examplefile.pdf?queryparam=value
        return documentLocation.getPath().endsWith(".pdf");
    }

    private void processLocation(final URL documentLocation) {
        log.debug("Starting to process document {}.", documentLocation);

        try {
            downloaderSemaphore.acquire();
        } catch (InterruptedException e) {
            log.error("Unable to acquire space in the download queue!", e);
        }

        downloaderExecutor.submit(() -> {
            try {
                documentDownloader.downloadDocument(documentLocation);
            } catch (Exception e) {
                log.debug("Failed to download document!", e);
            }

            downloaderSemaphore.release();
        });
    }

    private void shutdownGracefully() {
        log.info("Shutdown initialized for the downloader threads.");

        try {
            downloaderExecutor.shutdown();
            downloaderExecutor.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.error("Error occurred while shutting down the downloader threads.");
        }

        log.info("Shutdown finished for the downloader threads.");
    }
}
