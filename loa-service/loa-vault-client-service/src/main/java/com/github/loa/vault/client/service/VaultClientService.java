package com.github.loa.vault.client.service;

import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.vault.client.configuration.VaultClientConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VaultClientService {

    private final VaultClientConfigurationProperties vaultClientConfigurationProperties;

    public void createDocument(final DocumentEntity documentEntity, final InputStream documentContent)
            throws IOException {
        final MultipartRequest multipartRequest = new MultipartRequest("http://"
                + vaultClientConfigurationProperties.getHost() + ":" + vaultClientConfigurationProperties.getPort()
                + "/document/" + documentEntity.getId());

        multipartRequest.addFilePart("content", documentContent);

        int response = multipartRequest.execute();

        if (response != 200) {
            throw new RuntimeException("Unable to index document " + documentEntity.getId()
                    + "! Returned status code: " + response + ",");
        }
    }

    public InputStream queryDocument(final DocumentEntity documentEntity) {
        try {
            return new URL("http://" + vaultClientConfigurationProperties.getHost() + ":"
                    + vaultClientConfigurationProperties.getPort() + "/document/" + documentEntity.getId()).openStream();
        } catch (IOException e) {
            throw new RuntimeException("Unable to get content for document " + documentEntity.getId()
                    + " from vault!", e);
        }
    }
}
