package uk.gov.companieshouse.resourcechanged.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * The default service.
 */
@Component
class NullService implements ResourceChangedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NullService.class);

    @Override
    public void processMessage(ResourceChangedServiceParameters parameters) {
        final var message = parameters.getData();
        LOGGER.debug("Data in processMessage: {}", message);
        //TODO: Implement actual service class
    }
}