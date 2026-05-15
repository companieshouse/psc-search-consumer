package uk.gov.companieshouse.resourcechanged.service;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.stream.ResourceChangedData;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IdExtractorTest {

    @Test
    void extractPscId_shouldPrefixResourceId() {
        ResourceChangedData payload = mock(ResourceChangedData.class);
        when(payload.getResourceId()).thenReturn("123");

        IdExtractor extractor = new IdExtractor();
        String result = extractor.extractPscId(payload);

        assertEquals("pscid_123", result);
    }

    @Test
    void extractPscId_shouldHandleNullResourceId() {
        ResourceChangedData payload = mock(ResourceChangedData.class);
        when(payload.getResourceId()).thenReturn(null);

        IdExtractor extractor = new IdExtractor();
        String result = extractor.extractPscId(payload);

        assertEquals("pscid_null", result);
    }
}
