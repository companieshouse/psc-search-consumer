package uk.gov.companieshouse.service.resourcechanged;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.companieshouse.logging.DataMapHolder;
import uk.gov.companieshouse.model.PscSummary;
import uk.gov.companieshouse.stream.ResourceChangedData;
import uk.gov.companieshouse.client.PrimarySearchApiClient;

@Component
public class PscUpsertService implements ResourceChangedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PscUpsertService.class);
    private final PscDeserialiser deserialiser;
    private final PrimarySearchApiClient primarySearchApiClient;
    private final IdExtractor idExtractor;

    public PscUpsertService(PscDeserialiser deserialiser, PrimarySearchApiClient primarySearchApiClient, IdExtractor idExtractor) {
        this.deserialiser = deserialiser;
        this.primarySearchApiClient = primarySearchApiClient;
        this.idExtractor = idExtractor;
    }

    @Override
    public void processMessage(ResourceChangedData payload) {

        PscSummary pscSummary = deserialiser.deserialisePscData(payload.getData());
       
        String pscId = idExtractor.extractPscId(payload);
        // TO-DO Add pscId to structured logging 
        DataMapHolder.get().requestId(pscId);
        // Upsert PSC data to primary search API 
        primarySearchApiClient.upsertPsc(pscId, pscSummary);
        LOGGER.info("PSC index record upserted", DataMapHolder.getLogMap());
    }
    
    
}
