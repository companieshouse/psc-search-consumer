package uk.gov.companieshouse.resourcechanged.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.common.client.PrimarySearchApiClient;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import static uk.gov.companieshouse.Application.NAMESPACE;

@Component
public class PscSearchDeleteService implements ResourceChangedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final PrimarySearchApiClient apiClientService;

    public PscSearchDeleteService(PrimarySearchApiClient apiClientService) {
        this.apiClientService = apiClientService;
    }

    @Override
    public void processMessage(ResourceChangedServiceParameters parameters) {
        String resourceId = parameters.getData().getResourceId();

        LOGGER.info("Making API call to delete %s from PSC Search".formatted(resourceId));
        apiClientService.deletePsc(resourceId);
        LOGGER.info("Successfully deleted %s from PSC Search".formatted(resourceId));
    }
}
