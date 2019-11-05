package com.github.loa.indexer.service.index;

import com.github.loa.indexer.service.index.domain.DocumentMetadata;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IndexRequestFactory {

    public Mono<IndexRequest> newIndexRequest(final DocumentMetadata documentMetadata) {
        final Map<String, Object> sourceContent = new HashMap<>();

        sourceContent.put("content", documentMetadata.getContent().trim());

        if (documentMetadata.getTitle() != null && !documentMetadata.getTitle().isBlank()) {
            sourceContent.put("title", documentMetadata.getTitle().trim());
        }

        if (documentMetadata.getAuthor() != null && !documentMetadata.getAuthor().isBlank()) {
            sourceContent.put("author", documentMetadata.getAuthor().trim());
        }

        if (documentMetadata.getDate() != null && !documentMetadata.getDate().isBlank()) {
            sourceContent.put("date", documentMetadata.getDate().trim());
        }

        sourceContent.put("page_count", documentMetadata.getPageCount());

        return Mono.just(
                new IndexRequest("vault_documents")
                        .id(documentMetadata.getId())
                        .source(sourceContent)
                        .timeout(TimeValue.timeValueMinutes(30))
        );
    }
}
