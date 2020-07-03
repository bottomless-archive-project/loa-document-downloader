package com.github.loa.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.github.loa")
public class LibraryWebApplication {

    public static void main(final String[] args) {
        SpringApplication.run(LibraryWebApplication.class, args);
    }
}
