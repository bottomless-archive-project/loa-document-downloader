package com.github.loa.source.configuration.file;

import com.github.loa.source.service.file.domain.FileEncodingType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("loa.source.file")
public class FileDocumentSourceConfiguration {

    private String location;
    private FileEncodingType encoding;
}