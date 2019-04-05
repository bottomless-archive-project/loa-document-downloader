package com.github.loa.vault.service.location.file;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.configuration.location.file.FileConfigurationProperties;
import com.github.loa.vault.service.VaultLocationFactory;
import com.github.loa.vault.service.location.domain.VaultLocation;
import com.github.loa.vault.service.location.file.domain.FileVaultLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * A factory that create a local disc backed {@link VaultLocation} instances for documents.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "loa.vault.location.type", havingValue = "file")
public class FileVaultLocationFactory implements VaultLocationFactory {

    private final FileConfigurationProperties fileConfigurationProperties;

    /**
     * Create the location for a given document id.
     *
     * @param documentId the id of the entity to create the location for
     * @return the location of the document
     */
    public VaultLocation getLocation(final String documentId) {
        return new FileVaultLocation(
                new File(fileConfigurationProperties.getPath(), documentId + ".pdf")
        );
    }

    /**
     * Create the location for a given {@link DocumentEntity}.
     *
     * @param documentEntity the entity to create the location for
     * @return the location of the document
     */
    public VaultLocation getLocation(final DocumentEntity documentEntity) {
        return getLocation(documentEntity.getId());
    }
}