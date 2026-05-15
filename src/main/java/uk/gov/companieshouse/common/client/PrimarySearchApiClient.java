package uk.gov.companieshouse.common.client;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.psc.PscList;
import uk.gov.companieshouse.common.logging.DataMapHolder;

@Component
public class PrimarySearchApiClient {

    private static final String DELETE_PSC_API_CALL = "PSC Search API DELETE";
    private static final String SEARCH_API_PUT = "PSC Search API PUT";

    private final ApiClientService apiClientService;
    private final ResponseHandler responseHandler;

    public PrimarySearchApiClient(ApiClientService apiClientService, ResponseHandler responseHandler) {
        this.apiClientService = apiClientService;
        this.responseHandler = responseHandler;
    }

    public void deletePsc(String pscId) {
        String resourceUri = "/persons-with-significant-control-search/persons-with-significant-control/%s".formatted(pscId);
        InternalApiClient apiClient = apiClientService.getInternalApiClient();
        apiClient.getHttpClient().setRequestId(DataMapHolder.getRequestId());
        try {
             apiClient.privateSearchResourceHandler()
                     .pscSearch()
                     .delete(resourceUri)
                     .execute();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(DELETE_PSC_API_CALL, resourceUri, ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(DELETE_PSC_API_CALL, ex);
        }
    }

    public void upsertPsc(String pscId, PscList pscList) {
        String resourceUri = "/persons-with-significant-control-search/persons-with-significant-control/%s".formatted(pscId); 
        InternalApiClient apiClient = apiClientService.getInternalApiClient();
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
