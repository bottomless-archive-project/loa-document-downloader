package com.github.loa.document.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loa.document.service.domain.DocumentEntity;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is collect {@link DocumentEntity}s and able to say that an entity is already inserted or not. It persist
 * the collected information between runs. The collection is synchronized inside the class.
 */
@Slf4j
public class DocumentCollector {

    private final File documentCollectionLocation;
    private final ObjectMapper objectMapper;

    // The limit is ~2.1 billion documents handled because of this set
    private final Set<String> sets = new HashSet<>();

    public DocumentCollector(final File documentCollectionLocation, final ObjectMapper objectMapper) {
        this.documentCollectionLocation = documentCollectionLocation;
        this.objectMapper = objectMapper;

        if (documentCollectionLocation.exists()) {
            try {
                final Set<String> documents = objectMapper.readValue(documentCollectionLocation, Set.class);

                sets.addAll(documents);

                log.info("Loaded " + sets.size() + " document ids to the document collector.");
            } catch (IOException e) {
                log.error("Failed to load document id collection!", e);
            }
        }
    }

    public void insert(final DocumentEntity documentEntity) {
        synchronized (sets) {
            sets.add(documentEntity.getId());
        }
    }

    public boolean contains(final DocumentEntity documentEntity) {
        synchronized (sets) {
            return sets.contains(documentEntity.getId());
        }
    }

    public void persist() {
        try {
            synchronized (sets) {
                objectMapper.writeValue(documentCollectionLocation, sets);
            }
        } catch (IOException e) {
            log.error("Failed to persist document id collection!", e);
        }
    }
}
