package uk.gov.companieshouse.common.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.psc.PscList;

import static uk.gov.companieshouse.common.TestUtils.DELETE_PSC_API_CALL;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

@ExtendWith(MockitoExtension.class)
class PrimarySearchApiClientTest {

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private ResponseHandler responseHandler;

    private PrimarySearchApiClient primarySearchApiClient;
    private InternalApiClient apiClient;
    private static final String PSC_ID = "psc-id";
    private static final String RESOURCE_URI =
            String.format("/persons-with-significant-control-search/persons-with-significant-control/%s", PSC_ID);

    @BeforeEach
    void setUp() {
        primarySearchApiClient = new PrimarySearchApiClient(apiClientService, responseHandler);

        apiClient = mock(InternalApiClient.class, RETURNS_DEEP_STUBS);
        when(apiClientService.getInternalApiClient()).thenReturn(apiClient);
    }

    @Test
    void deletePscSuccessDelete() throws Exception {
        when(apiClient.privateSearchResourceHandler().pscSearch().delete(RESOURCE_URI).execute()).thenReturn(null);
        org.mockito.Mockito.clearInvocations(apiClient.privateSearchResourceHandler().pscSearch());

        primarySearchApiClient.deletePsc(PSC_ID);

        // verify delete called with expected uri
        verify(apiClient.privateSearchResourceHandler().pscSearch()).delete(RESOURCE_URI);
        verifyNoInteractions(responseHandler);
    }

    @Test
    void deletePscApiErrorResponseCallsResponseHandler() throws Exception {
        ApiErrorResponseException apiError = mock(ApiErrorResponseException.class);

        when(apiClient.privateSearchResourceHandler().pscSearch().delete(RESOURCE_URI).execute()).thenThrow(apiError);

        primarySearchApiClient.deletePsc(PSC_ID);

        verify(responseHandler).handle(DELETE_PSC_API_CALL, RESOURCE_URI, apiError);
    }

    @Test
    void deletePscUriValidationExceptionCallsResponseHandler() throws Exception {
        URIValidationException uriEx = mock(URIValidationException.class);

        when(apiClient.privateSearchResourceHandler().pscSearch().delete(RESOURCE_URI).execute()).thenThrow(uriEx);

        primarySearchApiClient.deletePsc(PSC_ID);

        verify(responseHandler).handle(DELETE_PSC_API_CALL, uriEx);
    }

    @Test
    void upsertPscSuccessfulPut() throws Exception {
        PscList pscList = mock(PscList.class);
        
        when(apiClient.privateSearchResourceHandler().pscSearch().put(RESOURCE_URI, pscList).execute()).thenReturn(null);
        org.mockito.Mockito.clearInvocations(apiClient.privateSearchResourceHandler().pscSearch());

        primarySearchApiClient.upsertPsc(PSC_ID, pscList);

        // verify put called with expected uri and payload
        verify(apiClient.privateSearchResourceHandler().pscSearch()).put(RESOURCE_URI, pscList);
        verifyNoInteractions(responseHandler);
    }

    @Test
    void upsertPscApiErrorResponseCallsResponseHandler() throws Exception {
        PscList pscList = mock(PscList.class);
        ApiErrorResponseException apiError = mock(ApiErrorResponseException.class);

        when(apiClient.privateSearchResourceHandler().pscSearch().put(RESOURCE_URI, pscList).execute()).thenThrow(apiError);

        primarySearchApiClient.upsertPsc(PSC_ID, pscList);

        verify(responseHandler).handle("PSC Search API PUT", RESOURCE_URI, apiError);
    }

    @Test
    void upsertUriValidationExceptionCallsResponseHandler() throws Exception {
        PscList pscList = mock(PscList.class);
        URIValidationException uriEx = mock(URIValidationException.class);

        when(apiClient.privateSearchResourceHandler().pscSearch().put(RESOURCE_URI, pscList).execute()).thenThrow(uriEx);

        primarySearchApiClient.upsertPsc(PSC_ID, pscList);

        verify(responseHandler).handle("PSC Search API PUT", uriEx);
    }

}
