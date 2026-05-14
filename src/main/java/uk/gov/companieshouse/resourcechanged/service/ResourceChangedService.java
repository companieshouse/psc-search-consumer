package uk.gov.companieshouse.resourcechanged.service;

import uk.gov.companieshouse.stream.ResourceChangedData;

public interface ResourceChangedService {

    void processMessage(ResourceChangedData changedData);
    
}
