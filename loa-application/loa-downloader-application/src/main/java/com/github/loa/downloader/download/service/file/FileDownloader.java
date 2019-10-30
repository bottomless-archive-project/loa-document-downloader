package com.github.loa.downloader.download.service.file;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * This service is responsible for downloading files from the internet.
 */
@Service
@RequiredArgsConstructor
public class FileDownloader {

    private final WebClient webClient;
    private final DefaultDataBufferFactory defaultDataBufferFactory;

    /**
     * Download a file from the provided url to the provided file location.
     *
     * @param downloadTarget the url to download the file from
     * @param resultLocation the location to download the file to
     */
    public Mono<File> downloadFile(final URL downloadTarget, final File resultLocation) {
        try {
            final Flux<DataBuffer> inputStream = webClient.get()
                    .uri(downloadTarget.toURI())
                    .retrieve()
                    .bodyToFlux(DataBuffer.class)
                    .onErrorReturn(defaultDataBufferFactory.allocateBuffer(0));

            DataBufferUtils.write(inputStream, resultLocation.toPath())
                    .block();

            return Mono.just(resultLocation);
        } catch (URISyntaxException e) {
            return Mono.empty();
        }
    }
}
