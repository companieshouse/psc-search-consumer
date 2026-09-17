package uk.gov.companieshouse.resourcechanged.serdes;

import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.api.psc.ListSummary;
import uk.gov.companieshouse.common.exception.PscDeserialisationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class PscDeserialiser {

    private static final Logger LOGGER = LoggerFactory.getLogger(PscDeserialiser.class);
    private final JsonMapper objectMapper;

    public PscDeserialiser(JsonMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ListSummary deserialiseListSummary(String data) {
        try {
            return objectMapper.readValue(data, ListSummary.class);
        } catch (JacksonException e) {
            LOGGER.error("Failed to parse PSC message payload: " + data, e);
            throw new PscDeserialisationException("PSC Deserialisation failed for data: " + data, e);
        }
    }
    
}
