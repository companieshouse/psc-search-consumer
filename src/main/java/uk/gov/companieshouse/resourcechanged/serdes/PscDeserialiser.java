package uk.gov.companieshouse.resourcechanged.serdes;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc_notifications.PscNotificationSummary;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
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

    public PscNotificationSummary deserialisePscNotificationSummary(String data) {
        try {
            return objectMapper.readValue(data, PscNotificationSummary.class);
        } catch (JacksonException e) {
            LOGGER.error("Failed to parse PSC message payload: " + data, e);
            throw new PscDeserialisationException("PSC Deserialisation failed for data: " + data, e);
        }
    }
    
}
