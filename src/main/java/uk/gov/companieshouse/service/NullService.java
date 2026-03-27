package uk.gov.companieshouse.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.util.ServiceParameters;
import uk.gov.companieshouse.exception.NonRetryableException;

/**
 * The default service.
 */
@Component
class NullService implements Service {

    @Override
    public void processMessage(ServiceParameters parameters) {
        throw new NonRetryableException("Unable to handle message");
    }
}