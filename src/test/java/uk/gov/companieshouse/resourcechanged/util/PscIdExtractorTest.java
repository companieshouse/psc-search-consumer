package uk.gov.companieshouse.resourcechanged.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.psc.ListSummary;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PscIdExtractorTest {

    private final PscIdExtractor extractor = new PscIdExtractor();

    private static final String PSC_ID = "UoS9J5Xq_wJn5y07NoZR_gaXqYA";
    private static final String NOTIFICATIONS_URL = "/persons-with-significant-control/" + PSC_ID + "/notifications";

    @Mock
    private ListSummary listSummary;

    @Test
    void shouldExtractPscIdFromNotificationsUrl() {
        Map<String, Object> pscLinksMap = new HashMap<>();
        pscLinksMap.put("notifications", NOTIFICATIONS_URL);

        Map<String, Object> linksMap = new HashMap<>();
        linksMap.put("persons_with_significant_control", pscLinksMap);

        when(listSummary.getLinks()).thenReturn(linksMap);
        Optional<String> result = extractor.extractPscId(listSummary);
        assertTrue(result.isPresent());
        assertEquals(PSC_ID, result.get());
    }

    @Test
    void shouldExtractPscIdFromNotificationsUrlWithHyphenatedLinksKey() {
        Map<String, Object> pscLinksMap = new HashMap<>();
        pscLinksMap.put("notifications", NOTIFICATIONS_URL);

        Map<String, Object> linksMap = new HashMap<>();
        linksMap.put("persons-with-significant-control", pscLinksMap);

        when(listSummary.getLinks()).thenReturn(linksMap);
        Optional<String> result = extractor.extractPscId(listSummary);
        assertTrue(result.isPresent());
        assertEquals(PSC_ID, result.get());
    }

    @Test
    void shouldReturnEmptyWhenListSummaryIsNull() {
        Optional<String> result = extractor.extractPscId(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenLinksAreNull() {
        when(listSummary.getLinks()).thenReturn(null);
        Optional<String> result = extractor.extractPscId(listSummary);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenPscLinksMapIsEmpty() {
        Map<String, Object> linksMap = new HashMap<>();
        when(listSummary.getLinks()).thenReturn(linksMap);
        Optional<String> result = extractor.extractPscId(listSummary);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenExceptionOccursDuringExtraction() {
        when(listSummary.getLinks()).thenThrow(new RuntimeException("Unexpected error"));
        Optional<String> result = extractor.extractPscId(listSummary);
        assertTrue(result.isEmpty());
    }
}
