package uk.gov.companieshouse.service.resourcechanged;

import uk.gov.companieshouse.stream.ResourceChangedData;

public interface ResourceChangedService {

    void processMessage(ResourceChangedData changedData);
    
}
