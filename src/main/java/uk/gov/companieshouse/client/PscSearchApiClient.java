package uk.gov.companieshouse.client;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.model.PscSummary;
import uk.gov.companieshouse.common.logging.DataMapHolder;
import uk.gov.companieshouse.client.ResponseHandler;
import uk.gov.companieshouse.api.psc.PscList;

@Component
public class PscSearchApiClient {

    private static final String SEARCH_API_PUT = "PSC Search API PUT";

    private final Supplier<InternalApiClient> apiClientSupplier;
    private final ResponseHandler responseHandler;

    public PscSearchApiClient(Supplier<InternalApiClient> apiClientSupplier, ResponseHandler responseHandler) {
        this.apiClientSupplier = apiClientSupplier;
        this.responseHandler = responseHandler;
    }

    public void upsertPsc(String pscId, PscList pscList) {
        String resourceUri = "/persons-with-significant-control-search/persons-with-significant-control/%s".formatted(pscId); 
        InternalApiClient apiClient = apiClientSupplier.get();
        apiClient.getHttpClient().setRequestId(DataMapHolder.getRequestId());
        try {
            apiClient.privateSearchResourceHandler()
                    .pscSearch()
                    .put(resourceUri, pscList)
                    .execute();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(SEARCH_API_PUT, resourceUri, ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(SEARCH_API_PUT, ex);
        }
    }
}