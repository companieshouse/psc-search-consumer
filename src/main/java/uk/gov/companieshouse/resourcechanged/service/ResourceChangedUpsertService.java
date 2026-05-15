package uk.gov.companieshouse.resourcechanged.service;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.companieshouse.common.logging.DataMapHolder;
import uk.gov.companieshouse.api.psc.ListSummary;
import uk.gov.companieshouse.api.psc.PscList;
import uk.gov.companieshouse.resourcechanged.serdes.PscDeserialiser;
import uk.gov.companieshouse.stream.ResourceChangedData;
import uk.gov.companieshouse.common.client.PrimarySearchApiClient;

@Component
public class ResourceChangedUpsertService implements ResourceChangedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceChangedUpsertService.class);
    private final PscDeserialiser deserialiser;
    private final PrimarySearchApiClient primarySearchApiClient;
    private final IdExtractor idExtractor;

    public ResourceChangedUpsertService(PscDeserialiser deserialiser, PrimarySearchApiClient primarySearchApiClient, IdExtractor idExtractor) {
        this.deserialiser = deserialiser;
        this.primarySearchApiClient = primarySearchApiClient;
        this.idExtractor = idExtractor;
    }

    @Override
    public void processMessage(ResourceChangedServiceParameters parameters) {

        ResourceChangedData payload = parameters.getData();
        
        ListSummary listSummary = deserialiser.deserialiseListSummary(payload.getData());
        String pscId = idExtractor.extractPscId(payload);

        PscList pscList = new PscList();
        pscList.setItems(java.util.Collections.singletonList(listSummary));
        pscList.setItemsPerPage(1);
        pscList.setStartIndex(0);
        pscList.setTotalResults(1);
        pscList.setActiveCount((listSummary.getCeased() != null && listSummary.getCeased()) ? 0 : 1);
        pscList.setCeasedCount((listSummary.getCeased() != null && listSummary.getCeased()) ? 1 : 0);
        pscList.setLinks(listSummary.getLinks());

        DataMapHolder.get().requestId(pscId);
        // Upsert PSC data to PSC search API 
        primarySearchApiClient.upsertPsc(pscId, pscList);
        LOGGER.info("PSC index record upserted", DataMapHolder.getLogMap());
    }
    
    
}
