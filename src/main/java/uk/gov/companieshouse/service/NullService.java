package uk.gov.companieshouse.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.util.ServiceParameters;
import uk.gov.companieshouse.exception.NonRetryableException;


/**
 * The default service.
 */
@Component
class NullService implements Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(NullService.class);

    @Override
    public void processMessage(ServiceParameters parameters) {
        final var message = parameters.getData();
        LOGGER.debug("Data in processMessage: {}", message);
        //TODO: Implement actual service class
    }
}