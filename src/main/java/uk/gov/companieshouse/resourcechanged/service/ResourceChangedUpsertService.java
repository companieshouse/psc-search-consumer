package uk.gov.companieshouse.resourcechanged.service;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.companieshouse.resourcechanged.logging.DataMapHolder;
import uk.gov.companieshouse.model.PscSummary;
import uk.gov.companieshouse.resourcechanged.serdes.PscDeserialiser;
import uk.gov.companieshouse.stream.ResourceChangedData;
import uk.gov.companieshouse.client.PscSearchApiClient;

@Component
public class ResourceChangedUpsertService implements ResourceChangedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceChangedUpsertService.class);
    private final PscDeserialiser deserialiser;
    private final PscSearchApiClient pscSearchApiClient;
    private final IdExtractor idExtractor;

    public ResourceChangedUpsertService(PscDeserialiser deserialiser, PscSearchApiClient pscSearchApiClient, IdExtractor idExtractor) {
        this.deserialiser = deserialiser;
        this.pscSearchApiClient = pscSearchApiClient;
        this.idExtractor = idExtractor;
    }

    @Override
    public void processMessage(ResourceChangedServiceParameters parameters) {

        ResourceChangedData payload = parameters.getData();
        PscSummary pscSummary = deserialiser.deserialisePscData(payload.getData());     
        String pscId = idExtractor.extractPscId(payload);

        DataMapHolder.get().requestId(pscId);
        // Upsert PSC data to PSC search API 
        pscSearchApiClient.upsertPsc(pscId, pscSummary);
        LOGGER.info("PSC index record upserted", DataMapHolder.getLogMap());
    }
    
    
}
