package com.github.loa.downloader;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@RequiredArgsConstructor
@SpringBootApplication(scanBasePackages = "com.github.loa")
@MapperScan(basePackages = "com.github.loa", annotationClass = Mapper.class)
public class LibraryDownloaderApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryDownloaderApplication.class, args);
    }
}
