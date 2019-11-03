package com.github.loa.document.service.location;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class DocumentLocationValidatorTest {

    private DocumentLocationValidator underTest;

    @BeforeEach
    private void setup() {
        underTest = new DocumentLocationValidator();
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "http://www.example.com/test.pdf",
                    "http://www.example.com/test.doc",
                    "http://www.example.com/test.docx",
                    "http://www.example.com/test.ppt",
                    "http://www.example.com/test.pptx",
                    "http://www.example.com/test.xls",
                    "http://www.example.com/test.xlsx",
                    "http://www.example.com/test.rtf",
                    "http://www.example.com/test.mobi",
                    "http://www.example.com/test.epub",
                    "http://www.example.com/test.epub?queryparam=value",
            }
    )
    void testValidDocumentLocationWithValidDocuments(final String documentLocation) throws MalformedURLException {
        final boolean result = underTest.validDocumentLocation(new URL(documentLocation));

        assertTrue(result);
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "http://www.example.com/test.exe",
                    "http://www.example.com/test.md",
                    "http://www.example.com/test",
            }
    )
    void testValidDocumentLocationWithInvalidDocuments(final String documentLocation) throws MalformedURLException {
        final boolean result = underTest.validDocumentLocation(new URL(documentLocation));

        assertFalse(result);
    }
}