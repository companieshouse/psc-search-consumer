package uk.gov.companieshouse.resourcechanged.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.psc.ListSummary;
import uk.gov.companieshouse.common.client.PrimarySearchApiClient;
import uk.gov.companieshouse.common.exception.NonRetryableException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.resourcechanged.serdes.PscDeserialiser;
import uk.gov.companieshouse.resourcechanged.util.PscIdExtractor;
import uk.gov.companieshouse.stream.ResourceChangedData;

import static uk.gov.companieshouse.Application.NAMESPACE;

@Component
public class PscSearchDeleteService implements ResourceChangedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final PrimarySearchApiClient apiClientService;
    private final PscIdExtractor pscIdExtractor;
    private final PscDeserialiser deserialiser;

    public PscSearchDeleteService(PrimarySearchApiClient apiClientService, PscIdExtractor pscIdExtractor,
                                  PscDeserialiser deserialiser) {
        this.apiClientService = apiClientService;
        this.pscIdExtractor = pscIdExtractor;
        this.deserialiser = deserialiser;
    }

    @Override
    public void processMessage(ResourceChangedServiceParameters parameters) {
        ResourceChangedData payload = parameters.getData();

        ListSummary listSummary = deserialiser.deserialiseListSummary(payload.getData());
        String resourceId = payload.getResourceId();
        String pscId = pscIdExtractor.extractPscId(listSummary)
                .orElseThrow(() -> {
                    LOGGER.error("Could not extract PSC ID from notifications link for delete request, resourceId: " + resourceId);
                    return new NonRetryableException(
                            "Could not extract PSC ID from notifications link for delete request, resourceId: " + resourceId);
                });

        LOGGER.info("Making API call to delete %s from PSC Search".formatted(pscId));
        apiClientService.deletePsc(pscId);
        LOGGER.info("Successfully deleted %s from PSC Search".formatted(pscId));
    }
}
