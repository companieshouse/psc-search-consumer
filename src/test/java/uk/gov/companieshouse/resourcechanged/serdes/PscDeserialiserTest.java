package uk.gov.companieshouse.resourcechanged.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.psc_notifications.PscNotificationSummary;
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
    void deserialisePscNotificationSummarySuccess() throws Exception {
        String json = "{\"field\":\"value\"}";
        PscNotificationSummary expected = mock(PscNotificationSummary.class);
        when(objectMapper.readValue(json, PscNotificationSummary.class)).thenReturn(expected);

        PscNotificationSummary result = deserialiser.deserialisePscNotificationSummary(json);
        assertSame(expected, result);
    }

    @Test
    void deserialisePscNotificationSummaryThrowsPscDeserialisationException() throws Exception {
        String json = "xyz";
        when(objectMapper.readValue(json, PscNotificationSummary.class)).thenThrow(new JsonProcessingException("fail"){});

        PscDeserialisationException ex = assertThrows(PscDeserialisationException.class, () ->
                deserialiser.deserialisePscNotificationSummary(json));
        assertTrue(ex.getMessage().contains("PSC Deserialisation failed for data: " + json));
    }
}
