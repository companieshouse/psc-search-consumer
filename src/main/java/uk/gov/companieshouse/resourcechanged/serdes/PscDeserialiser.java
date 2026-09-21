package uk.gov.companieshouse.resourcechanged.serdes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc_notifications.PscNotificationSummary;
import uk.gov.companieshouse.common.exception.PscDeserialisationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class PscDeserialiser {

    private static final Logger LOGGER = LoggerFactory.getLogger(PscDeserialiser.class);
    private final ObjectMapper objectMapper;

    public PscDeserialiser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public PscNotificationSummary deserialisePscNotificationSummary(String data) {
        try {
            return objectMapper.readValue(data, PscNotificationSummary.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to parse PSC message payload: " + data, e);
            throw new PscDeserialisationException("PSC Deserialisation failed for data: " + data, e);
        }
    }
    
}
