package uk.gov.companieshouse.resourcechanged.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.psc.ListSummary;
import uk.gov.companieshouse.common.exception.PscDeserialisationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PscDeserialiserTest {

    private ObjectMapper objectMapper;
    private PscDeserialiser deserialiser;

    @BeforeEach
    void setUp() {
        objectMapper = mock(ObjectMapper.class);
        deserialiser = new PscDeserialiser(objectMapper);
    }

    @Test
    void deserialiseListSummarySuccess() throws Exception {
        String json = "{\"field\":\"value\"}";
        ListSummary expected = mock(ListSummary.class);
        when(objectMapper.readValue(json, ListSummary.class)).thenReturn(expected);

        ListSummary result = deserialiser.deserialiseListSummary(json);
        assertSame(expected, result);
    }

    @Test
    void deserialiseListSummaryThrowsPscDeserialisationException() throws Exception {
        String json = "xyz";
        when(objectMapper.readValue(json, ListSummary.class)).thenThrow(new JsonProcessingException("fail"){});

        PscDeserialisationException ex = assertThrows(PscDeserialisationException.class, () ->
                deserialiser.deserialiseListSummary(json));
        assertTrue(ex.getMessage().contains("Failed to parse PSC message payload for data: " + json));
    }
}
