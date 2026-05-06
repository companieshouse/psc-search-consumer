package uk.gov.companieshouse.service.resourcechanged;

import uk.gov.companieshouse.stream.ResourceChangedData;

public class IdExtractor {

    /**
     * Extracts the PSC ID with prefix added
     * @param payload ResourceChangedData event
     * @return prefixed PSC ID 
     */
    public String extractPscId(ResourceChangedData payload) {
        return "pscid_" + payload.getResourceId();
    }
    
}
