package uk.gov.companieshouse.service;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.companieshouse.logging.DataMapHolder;
import uk.gov.companieshouse.model.PscSummary;
import uk.gov.companieshouse.stream.ResourceChangedData;

//TO-DO Add structured logging

@Component
public class PscResourceChangedUpsertService implements ResourceChangedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PscResourceChangedUpsertService.class);
    private final PscDeserialiser deserialiser;
    private final SearchApiClient searchApiClient;
    private final IdExtractor idExtractor;

    public PscResourceChangedUpsertService(PscDeserialiser deserialiser, SearchApiClient searchApiClient, IdExtractor idExtractor) {
        this.deserialiser = deserialiser;
        this.searchApiClient = searchApiClient;
        this.idExtractor = idExtractor;
    }

    @Override
    public void processMessage(ResourceChangedData payload) {

        PscSummary pscSummary = deserialiser.deserialisePscData(payload.getData());
       
        String pscId = idExtractor.extractPscId(payload);
        // TO-DO Add pscId to structured logging 
        DataMapHolder.get().requestId(pscId);
        // Upsert PSC data to search API 
        searchApiClient.upsertPscSummary(pscId, pscSummary);
        LOGGER.info("PSC index record upserted", DataMapHolder.getLogMap());
    }
}
