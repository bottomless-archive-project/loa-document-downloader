package com.github.loa.document.repository;

import com.github.loa.document.repository.domain.DocumentDatabaseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DocumentRepository {

    private final ReactiveMongoTemplate mongoTemplate;

    public Mono<DocumentDatabaseEntity> insertDocument(final DocumentDatabaseEntity documentDatabaseEntity) {
        return mongoTemplate.insert(documentDatabaseEntity);
    }

    public Mono<DocumentDatabaseEntity> findById(final String documentId) {
        return mongoTemplate.findById(documentId, DocumentDatabaseEntity.class);
    }

    public Flux<DocumentDatabaseEntity> findByStatus(final String status) {
        final Query query = Query
                .query(
                        Criteria.where("status").is(status)
                );

        return mongoTemplate.find(query, DocumentDatabaseEntity.class);
    }

    public Mono<Void> updateStatus(final String documentId, final String status) {
        final Query query = Query
                .query(
                        Criteria.where("id").is(documentId)
                );

        return mongoTemplate.updateFirst(query, Update.update("status", status), DocumentDatabaseEntity.class).then();
    }

    public void updateCompression(final String documentId, final String compression) {
        final Query query = Query
                .query(
                        Criteria.where("id").is(documentId)
                );

        mongoTemplate.updateFirst(query, Update.update("compression", compression), DocumentDatabaseEntity.class)
                .subscribe();
    }

    public Mono<Boolean> existsByChecksumAndFileSize(final String checksum, final long fileSize,
            final String type) {
        final Query query = Query
                .query(
                        Criteria.where("checksum").is(checksum)
                                .and("fileSize").is(fileSize)
                                .and("type").is(type)
                );

        return mongoTemplate.exists(query, DocumentDatabaseEntity.class);
    }

    public Mono<Void> updatePageCount(final String documentId, final int pageCount) {
        final Query query = Query
                .query(
                        Criteria.where("id").is(documentId)
                );

        return mongoTemplate.updateFirst(query, Update.update("pageCount", pageCount), DocumentDatabaseEntity.class)
                .then();
    }

    public Flux<DocumentDatabaseEntity> findAll() {
        return mongoTemplate.findAll(DocumentDatabaseEntity.class);
    }

    public Mono<Long> count() {
        return mongoTemplate.count(Query.query(new Criteria()), DocumentDatabaseEntity.class);
    }
}
