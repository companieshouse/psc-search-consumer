package uk.gov.companieshouse.resourcechanged.service;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.companieshouse.common.exception.NonRetryableException;
import uk.gov.companieshouse.common.logging.DataMapHolder;
import uk.gov.companieshouse.api.psc.ListSummary;
import uk.gov.companieshouse.api.psc.PscList;
import uk.gov.companieshouse.resourcechanged.serdes.PscDeserialiser;
import uk.gov.companieshouse.resourcechanged.util.PscIdExtractor;
import uk.gov.companieshouse.stream.ResourceChangedData;
import uk.gov.companieshouse.common.client.PrimarySearchApiClient;

@Component
public class PscSearchUpsertService implements ResourceChangedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PscSearchUpsertService.class);
    private final PscDeserialiser deserialiser;
    private final PrimarySearchApiClient primarySearchApiClient;
    private final PscIdExtractor pscIdExtractor;

    public PscSearchUpsertService(PscDeserialiser deserialiser, PrimarySearchApiClient primarySearchApiClient, PscIdExtractor pscIdExtractor) {
        this.deserialiser = deserialiser;
        this.primarySearchApiClient = primarySearchApiClient;
        this.pscIdExtractor = pscIdExtractor;
    }

    @Override
    public void processMessage(ResourceChangedServiceParameters parameters) {

        ResourceChangedData payload = parameters.getData();
        
        ListSummary listSummary = deserialiser.deserialiseListSummary(payload.getData());
        String resourceId = payload.getResourceId();
        String pscId = pscIdExtractor.extractPscId(listSummary)
                .orElseThrow(() -> {
                    LOGGER.error("Could not extract PSC ID from notifications link, resourceId: {}", resourceId);
                    return new NonRetryableException(
                            "Could not extract PSC ID from notifications link for resourceId: " + resourceId);
                });

        PscList pscList = new PscList();
        pscList.setItems(java.util.Collections.singletonList(listSummary));
        pscList.setItemsPerPage(1);
        pscList.setStartIndex(0);
        pscList.setTotalResults(1);
        pscList.setActiveCount((listSummary.getCeased() != null && listSummary.getCeased()) ? 0 : 1);
        pscList.setCeasedCount((listSummary.getCeased() != null && listSummary.getCeased()) ? 1 : 0);
        pscList.setLinks(listSummary.getLinks());

        DataMapHolder.get().requestId(pscId);
        primarySearchApiClient.upsertPsc(pscId, pscList);
        LOGGER.info("PSC index record upserted: {}", DataMapHolder.getLogMap());
    }
    
    
}
