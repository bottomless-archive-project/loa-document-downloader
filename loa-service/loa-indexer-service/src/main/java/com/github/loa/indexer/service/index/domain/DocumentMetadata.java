package com.github.loa.indexer.service.index.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentMetadata {

    private final String id;
    private final String content;
    private final String title;
    private final String author;
    private final String date;
    private final String language;
    private final int pageCount;
}
