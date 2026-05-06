package uk.gov.companieshouse.client;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.model.PscSummary;
import uk.gov.companieshouse.logging.DataMapHolder;
import uk.gov.companieshouse.service.ResponseHandler;

@Component
public class PrimarySearchApiClient {

    private static final String SEARCH_API_PUT = "Primary Search API PUT";

    private final Supplier<InternalApiClient> apiClientSupplier;
    private final ResponseHandler responseHandler;

    public PrimarySearchApiClient(Supplier<InternalApiClient> apiClientSupplier, ResponseHandler responseHandler) {
        this.apiClientSupplier = apiClientSupplier;
        this.responseHandler = responseHandler;
    }

    public void upsertPsc(String pscId, PscSummary pscSummary) {
        String resourceUri = "/search/psc/%s".formatted(pscId); 
        InternalApiClient apiClient = apiClientSupplier.get();
        apiClient.getHttpClient().setRequestId(DataMapHolder.getRequestId());
        try {
            // TO-DO - Replace actual resource handler
            apiClient.primarySearchResourceHandler()
                    .pscSearch()
                    .put(resourceUri, pscSummary)
                    .execute();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(SEARCH_API_PUT, resourceUri, ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(SEARCH_API_PUT, ex);
        }
    }
}