package uk.gov.companieshouse.resourcechanged.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.model.PscSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class PscDeserialiser {

    private static final Logger LOGGER = LoggerFactory.getLogger(PscDeserialiser.class);
    private final ObjectMapper objectMapper;

    public PscDeserialiser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PscSummary deserialisePscData(String data) {
        try {
            return objectMapper.readValue(data, PscSummary.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to parse PSC message payload", e);
            throw new RuntimeException("Failed to parse PSC message payload", e);
        }
    }
    
}
