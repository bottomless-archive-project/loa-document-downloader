package com.github.loa.administrator.command.compressor;

import com.github.loa.compression.configuration.CompressionConfigurationProperties;
import com.github.loa.document.service.domain.DocumentEntity;
import com.github.loa.document.service.entity.factory.DocumentEntityFactory;
import com.github.loa.vault.client.service.VaultClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("silent-compressor")
public class SilentCompressorCommand implements CommandLineRunner {

    private final DocumentEntityFactory documentEntityFactory;
    private final CompressionConfigurationProperties compressionConfigurationProperties;
    private final VaultClientService vaultClientService;

    @Override
    public void run(final String... args) {
        documentEntityFactory.getDocumentEntities()
                .parallel(10)
                .runOn(Schedulers.parallel())
                .filter(DocumentEntity::isArchived)
                .filter(this::shouldCompress)
                .doOnNext(documentEntity -> vaultClientService.recompressDocument(documentEntity,
                        compressionConfigurationProperties.getAlgorithm()));
    }

    private boolean shouldCompress(final DocumentEntity documentEntity) {
        return documentEntity.getCompression() != compressionConfigurationProperties.getAlgorithm();
    }
}
