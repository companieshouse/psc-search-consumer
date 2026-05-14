package uk.gov.companieshouse.common.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
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
    private static final String pscId = "psc-id";
    private static final String resourceUri =
            String.format("/persons-with-significant-control-search/persons-with-significant-control/%s", pscId);

    @BeforeEach
    void setUp() {
        primarySearchApiClient = new PrimarySearchApiClient(apiClientService, responseHandler);

        apiClient = mock(InternalApiClient.class, RETURNS_DEEP_STUBS);
        when(apiClientService.getInternalApiClient()).thenReturn(apiClient);
    }

    @Test
    void deletePscSuccessDelete() throws Exception {
        when(apiClient.privateSearchResourceHandler().pscSearch().delete(resourceUri).execute()).thenReturn(null);
        org.mockito.Mockito.clearInvocations(apiClient.privateSearchResourceHandler().pscSearch());

        primarySearchApiClient.deletePsc(pscId);

        // verify delete called with expected uri
        verify(apiClient.privateSearchResourceHandler().pscSearch()).delete(resourceUri);
        verifyNoInteractions(responseHandler);
    }

    @Test
    void deletePscApiErrorResponseCallsResponseHandler() throws Exception {
        ApiErrorResponseException apiError = mock(ApiErrorResponseException.class);

        when(apiClient.privateSearchResourceHandler().pscSearch().delete(resourceUri).execute()).thenThrow(apiError);

        primarySearchApiClient.deletePsc(pscId);

        verify(responseHandler).handle(DELETE_PSC_API_CALL, resourceUri, apiError);
    }

    @Test
    void deletePscUriValidationExceptionCallsResponseHandler() throws Exception {
        URIValidationException uriEx = mock(URIValidationException.class);

        when(apiClient.privateSearchResourceHandler().pscSearch().delete(resourceUri).execute()).thenThrow(uriEx);

        primarySearchApiClient.deletePsc(pscId);

        verify(responseHandler).handle(DELETE_PSC_API_CALL, uriEx);
    }
}
