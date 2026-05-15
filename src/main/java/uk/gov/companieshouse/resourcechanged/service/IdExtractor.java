package uk.gov.companieshouse.resourcechanged.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.stream.ResourceChangedData;

@Component
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
