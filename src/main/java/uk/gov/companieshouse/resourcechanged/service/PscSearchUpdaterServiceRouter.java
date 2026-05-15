package uk.gov.companieshouse.resourcechanged.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.common.exception.NonRetryableException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import static uk.gov.companieshouse.Application.NAMESPACE;

@Service
public class PscSearchUpdaterServiceRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final PscSearchDeleteService pscSearchDeleteService;
    private final PscSearchUpsertService pscSearchUpsertService;

    public PscSearchUpdaterServiceRouter(PscSearchDeleteService pscSearchDeleteService, PscSearchUpsertService pscSearchUpsertService) {
        this.pscSearchDeleteService = pscSearchDeleteService;
        this.pscSearchUpsertService = pscSearchUpsertService;
    }

    public void route(ResourceChangedServiceParameters parameters) {

        final var message = parameters.getData();
        final var resourceId = message.getResourceId();
        final var resourceKind = message.getResourceKind();
        final var resourceUri = message.getResourceUri();

        LOGGER.info("Processing message %s for resource ID %s, resource kind %s, resource URI %s".formatted(message, resourceId, resourceKind, resourceUri));

        var messageType = message.getEvent().getType();

        switch (messageType) {
            case "changed":
                LOGGER.debug("This is a 'changed' type message.");
                pscSearchUpsertService.processMessage(parameters);
                break;
            case "deleted":
                LOGGER.debug("This is a 'deleted' type message.");
                pscSearchDeleteService.processMessage(parameters);
                break;
            default:
                LOGGER.error(String.format("NonRetryable error occurred, unknown message type of %s", messageType));
                throw new NonRetryableException(
                        String.format("Unknown message type of %s with resourceId of %s", messageType, resourceId));
        }
    }
}
