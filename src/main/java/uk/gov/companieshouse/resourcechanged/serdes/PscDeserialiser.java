package uk.gov.companieshouse.resourcechanged.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc.ListSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class PscDeserialiser {

    private static final Logger LOGGER = LoggerFactory.getLogger(PscDeserialiser.class);
    private final ObjectMapper objectMapper;

    public PscDeserialiser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ListSummary deserialiseListSummary(String data) {
        try {
            return objectMapper.readValue(data, ListSummary.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to parse PSC message payload", e);
            throw new RuntimeException("Failed to parse PSC message payload", e);
        }
    }
    
}
