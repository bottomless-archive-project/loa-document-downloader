package com.github.loa.source.file.service.location;

import com.github.loa.location.domain.DocumentLocation;
import com.github.loa.source.configuration.DocumentSourceConfiguration;
import com.github.loa.source.file.configuration.FileDocumentSourceConfigurationProperties;
import com.github.loa.source.file.service.FileSourceFactory;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.BufferedReader;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileDocumentLocationFactoryTest {

    @Mock
    private FileDocumentSourceConfigurationProperties fileDocumentSourceConfigurationProperties;

    @Mock
    private FileSourceFactory fileSourceFactory;

    @Mock
    private Counter processedDocumentLocationCount;

    @Mock
    private BufferedReaderAdapter adapter;

    @Mock
    private DocumentSourceConfiguration documentSourceConfiguration;

    @InjectMocks
    private FileDocumentLocationSource underTest;

    @Test
    void testWhenSkipLinesAreNegative() {
        when(fileDocumentSourceConfigurationProperties.getSkipLines())
                .thenReturn(-1L);

        assertThrows(IllegalArgumentException.class, () -> underTest.streamLocations());
    }

    @Test
    void testWhenSkipLinesAreSet() {
        when(fileDocumentSourceConfigurationProperties.getSkipLines())
                .thenReturn(3L);
        final BufferedReader reader = mock(BufferedReader.class);
        when(fileSourceFactory.newSourceReader())
                .thenReturn(reader);
        when(adapter.consume())
                .thenReturn((newReader) -> {
                    assertThat(reader, is(newReader));

                    return Flux.fromIterable(List.of("http://www.example.com/1", "http://www.example.com/2", "http://www.example.com/3",
                            "http://www.example.com/4", "http://www.example.com/5"));
                });
        when(adapter.close())
                .thenReturn((newReader) -> {
                });

        final Flux<DocumentLocation> result = underTest.streamLocations();

        StepVerifier.create(result)
                .expectNextCount(2)
                .verifyComplete();

        verify(processedDocumentLocationCount, times(2)).increment();
    }

    @Test
    void testWhenSkipLinesAreNotSet() {
        final BufferedReader reader = mock(BufferedReader.class);
        when(fileSourceFactory.newSourceReader())
                .thenReturn(reader);
        when(adapter.consume())
                .thenReturn((newReader) -> {
                    assertThat(reader, is(newReader));

                    return Flux.fromIterable(List.of("http://www.example.com/1", "http://www.example.com/2", "http://www.example.com/3",
                            "http://www.example.com/4", "http://www.example.com/5"));
                });
        when(adapter.close())
                .thenReturn((newReader) -> {
                });
        when(documentSourceConfiguration.getName())
                .thenReturn("test-source");

        final Flux<DocumentLocation> result = underTest.streamLocations();

        StepVerifier.create(result)
                .consumeNextWith(documentLocation -> {
                    assertTrue(documentLocation.getLocation().toUrl().isPresent());
                    assertEquals("http://www.example.com/1", documentLocation.getLocation().toUrl().get().toString());
                    assertEquals("test-source", documentLocation.getSourceName());
                })
                .consumeNextWith(documentLocation -> {
                    assertTrue(documentLocation.getLocation().toUrl().isPresent());
                    assertEquals("http://www.example.com/2", documentLocation.getLocation().toUrl().get().toString());
                    assertEquals("test-source", documentLocation.getSourceName());
                })
                .consumeNextWith(documentLocation -> {
                    assertTrue(documentLocation.getLocation().toUrl().isPresent());
                    assertEquals("http://www.example.com/3", documentLocation.getLocation().toUrl().get().toString());
                    assertEquals("test-source", documentLocation.getSourceName());
                })
                .consumeNextWith(documentLocation -> {
                    assertTrue(documentLocation.getLocation().toUrl().isPresent());
                    assertEquals("http://www.example.com/4", documentLocation.getLocation().toUrl().get().toString());
                    assertEquals("test-source", documentLocation.getSourceName());
                })
                .consumeNextWith(documentLocation -> {
                    assertTrue(documentLocation.getLocation().toUrl().isPresent());
                    assertEquals("http://www.example.com/5", documentLocation.getLocation().toUrl().get().toString());
                    assertEquals("test-source", documentLocation.getSourceName());
                })
                .verifyComplete();

        verify(processedDocumentLocationCount, times(5)).increment();
    }
}
