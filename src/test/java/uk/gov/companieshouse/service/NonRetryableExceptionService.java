package uk.gov.companieshouse.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.exception.NonRetryableException;
import uk.gov.companieshouse.util.ServiceParameters;

@Component
public class NonRetryableExceptionService implements Service {

    @Override
    public void processMessage(ServiceParameters parameters) {
        throw new NonRetryableException("Unable to handle message",
                new Exception("Unable to handle message"));
    }
}