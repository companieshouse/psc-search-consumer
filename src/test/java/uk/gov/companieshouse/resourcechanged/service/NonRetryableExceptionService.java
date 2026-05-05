package uk.gov.companieshouse.resourcechanged.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.common.exception.NonRetryableException;
import uk.gov.companieshouse.resourcechanged.service.ResourceChangedService;
import uk.gov.companieshouse.resourcechanged.service.ResourceChangedServiceParameters;

@Component
public class NonRetryableExceptionService implements ResourceChangedService {

    @Override
    public void processMessage(ResourceChangedServiceParameters parameters) {
        throw new NonRetryableException("Unable to handle message",
                new Exception("Unable to handle message"));
    }
}