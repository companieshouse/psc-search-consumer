package uk.gov.companieshouse.resourcechanged.serdes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.api.psc.ListSummary;
import uk.gov.companieshouse.common.exception.PscDeserialisationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PscDeserialiserTest {

    private JsonMapper objectMapper;
    private PscDeserialiser deserialiser;

    @BeforeEach
    void setUp() {
        objectMapper = mock(JsonMapper.class);
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
        when(objectMapper.readValue(json, ListSummary.class)).thenThrow(new StreamReadException("fail"));

        PscDeserialisationException ex = assertThrows(PscDeserialisationException.class, () ->
                deserialiser.deserialiseListSummary(json));
        assertTrue(ex.getMessage().contains("PSC Deserialisation failed for data: " + json));
    }
}
