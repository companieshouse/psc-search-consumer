package uk.gov.companieshouse.service;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;

import uk.gov.companieshouse.model.PscSummary;
import uk.gov.companieshouse.logging.DataMapHolder;

@Component
public class SearchApiClient {

    private static final String SEARCH_API_PUT = "PSC Search API PUT";

    private final Supplier<InternalApiClient> apiClientSupplier;
    private final ResponseHandler responseHandler;

    public SearchApiClient(Supplier<InternalApiClient> apiClientSupplier, ResponseHandler responseHandler) {
        this.apiClientSupplier = apiClientSupplier;
        this.responseHandler = responseHandler;
    }

    public void upsertPscSummary(String pscId, PscSummary pscSummary) {
        String resourceUri = "/psc-search/psc/%s".formatted(pscId);
        InternalApiClient apiClient = apiClientSupplier.get();
        apiClient.getHttpClient().setRequestId(DataMapHolder.getRequestId());
        try {
            apiClient.privateSearchResourceHandler()
                    // TO-DO - Add PSC Search to SDK
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
