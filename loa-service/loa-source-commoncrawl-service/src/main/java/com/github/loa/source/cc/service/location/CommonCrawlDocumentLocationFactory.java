package com.github.loa.source.cc.service.location;

import com.github.loa.source.cc.service.WarcDownloader;
import com.github.loa.source.cc.service.WarcFluxFactory;
import com.github.loa.source.cc.service.WarcRecordParser;
import com.github.loa.source.service.DocumentLocationFactory;
import com.github.loa.url.service.URLConverter;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.List;

/**
 * A {@link DocumentLocationFactory} that generates locations for parsable items. The items are collected from the
 * <a href="https://commoncrawl.org/the-data/get-started/">Common Crawl</a> corpus.
 */
@Slf4j
@RequiredArgsConstructor
public class CommonCrawlDocumentLocationFactory implements DocumentLocationFactory {

    private final WarcDownloader warcDownloader;
    private final WarcRecordParser warcRecordParser;
    private final WarcFluxFactory warcFluxFactory;
    private final URLConverter urlConverter;
    private final List<String> paths;
    private final Counter processedDocumentLocationCount;

    @Override
    public Flux<URL> streamLocations() {
        return Flux.fromIterable(paths)
                .flatMap(warcLocation ->
                        Mono.just(warcLocation)
                                .flatMap(warcDownloader::downloadWarcFile)
                                .flatMapMany(warcFluxFactory::buildWarcRecordFlux)
                                .flatMap(warcRecordParser::parseUrlsFromRecord)
                                .doOnNext(line -> processedDocumentLocationCount.increment())
                                .flatMap(urlConverter::convert)
                                .doOnError(error -> log.error("Failed to download WARC location {}!", warcLocation, error))
                                .retry()
                );
    }
}
