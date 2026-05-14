package uk.gov.companieshouse.resourcechanged.service;

import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.common.exception.NonRetryableException;
import uk.gov.companieshouse.stream.ResourceChangedData;

@Service
public class ResourceChangedServiceRouter {


    private static final String EVENT_CHANGED = "changed";

    private final ResourceChangedUpsertService resourceChangedUpsertService;

    ResourceChangedServiceRouter(ResourceChangedUpsertService resourceChangedUpsertService) {
        this.resourceChangedUpsertService = resourceChangedUpsertService;
    }

    public void route(Message<ResourceChangedData> message) {
        ResourceChangedData payload = message.getPayload();

        if (EVENT_CHANGED.equals(payload.getEvent().getType())) {
            resourceChangedUpsertService.processMessage(new ResourceChangedServiceParameters(payload));
        } else {
            // Future delete event handling can be added here - for now throw exception for unsupported event types
            throw new NonRetryableException(
                    String.format("Unable to handle message with log context [%s]", payload.getContextId()));
        }
    }
    
}
