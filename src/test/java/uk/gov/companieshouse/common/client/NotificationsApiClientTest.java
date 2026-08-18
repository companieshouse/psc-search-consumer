package uk.gov.companieshouse.common.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiError;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.notification.PrivateCompanyNotification;
import uk.gov.companieshouse.api.handler.notification.PrivateCompanyNotificationsListHandler;
import uk.gov.companieshouse.api.handler.psc.PrivatePscNotificationsListGet;
import uk.gov.companieshouse.api.handler.psc.PrivatePscNotificationsListHandler;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.psc_notifications.NotificationList;
import uk.gov.companieshouse.api.psc_notifications.PscNotificationSummary;
import uk.gov.companieshouse.api.request.QueryParam;

import javax.management.Query;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@ExtendWith(MockitoExtension.class)
class NotificationsApiClientTest {

    @Mock
    private Supplier<InternalApiClient> clientSupplier;
    @Mock
    private ResponseHandler responseHandler;
    @InjectMocks
    private NotificationsApiClient client;
    @Mock
    private InternalApiClient apiClient;
    @Mock
    private HttpClient httpClient;
    @Mock
    private PrivateCompanyNotificationsListHandler privateCompanyNotificationsListHandler;
    @Mock
    private PrivateCompanyNotification privateCompanyNotification;
    @Mock
    private PscNotificationSummary pscNotificationSummary;
    @Mock
    private PrivatePscNotificationsListHandler notificationsListHandler;
    @Mock
    private PrivatePscNotificationsListGet privatePscNotificationsListGet;
    @Mock
    private NotificationList notificationList;

    @Captor
    private ArgumentCaptor<List<QueryParam>> queryParamCaptor;

    @BeforeEach
    void setup() {
        when(clientSupplier.get()).thenReturn(apiClient);
        when(apiClient.getHttpClient()).thenReturn(httpClient);
    }

    @Test
    void shouldFetchNotification() throws ApiErrorResponseException, URIValidationException {
        //given
        when(apiClient.privateCompanyNotificationsListHandler()).thenReturn(privateCompanyNotificationsListHandler);
        when(privateCompanyNotificationsListHandler.getPscNotification(any())).thenReturn(privateCompanyNotification);
        when(privateCompanyNotification.execute()).thenReturn(
                new ApiResponse<>(200, Collections.emptyMap(), pscNotificationSummary));

        //when
        Optional<PscNotificationSummary> actual = client.getNotification(
                "/company/12345678/notifications/987ihg654fed321cba");

        //then
        assertTrue(actual.isPresent());
        assertEquals(pscNotificationSummary, actual.get());
        verify(privateCompanyNotificationsListHandler).getPscNotification(
                "/company/12345678/notifications/987ihg654fed321cba");
    }

    @Test
    void shouldReturnEmptyOptionalGetNotification404NotFound()
            throws ApiErrorResponseException, URIValidationException {
        //given
        HttpResponseException.Builder builder = new HttpResponseException.Builder(404,
                "not found", new HttpHeaders());
        ApiErrorResponseException apiErrorResponseException = new ApiErrorResponseException(builder);

        when(apiClient.privateCompanyNotificationsListHandler()).thenReturn(privateCompanyNotificationsListHandler);
        when(privateCompanyNotificationsListHandler.getPscNotification(any())).thenReturn(privateCompanyNotification);
        when(privateCompanyNotification.execute()).thenThrow(apiErrorResponseException);

        //when
        Optional<PscNotificationSummary> actual = client.getNotification(
                "/company/12345678/notifications/987ihg654fed321cba");

        //then
        assertTrue(actual.isEmpty());
        verify(privateCompanyNotificationsListHandler).getPscNotification(
                "/company/12345678/notifications/987ihg654fed321cba");
        verifyNoInteractions(responseHandler);
    }

    @Test
    void getNotificationApiErrorResponseException() throws ApiErrorResponseException, URIValidationException {
        //given
        HttpResponseException.Builder builder = new HttpResponseException.Builder(503,
                "service unavailable", new HttpHeaders());
        ApiErrorResponseException apiErrorResponseException = new ApiErrorResponseException(builder);

        //when
        when(apiClient.privateCompanyNotificationsListHandler()).thenReturn(privateCompanyNotificationsListHandler);
        when(privateCompanyNotificationsListHandler.getPscNotification(any())).thenReturn(privateCompanyNotification);
        when(privateCompanyNotification.execute()).thenThrow(apiErrorResponseException);

        client.getNotification("/company/12345678/notifications/987ihg654fed321cba");

        //then
        verify(privateCompanyNotificationsListHandler).getPscNotification(
                "/company/12345678/notifications/987ihg654fed321cba");
        verify(responseHandler).handle("Notifications API GET Notification",
                "/company/12345678/notifications/987ihg654fed321cba", apiErrorResponseException);
    }

    @Test
    void getNotificationURIValidationException()
        throws ApiErrorResponseException, URIValidationException {
        //given
        URIValidationException uriValidationException = new URIValidationException("Invalid URI");
        when(apiClient.privateCompanyNotificationsListHandler()).thenReturn(privateCompanyNotificationsListHandler);
        when(privateCompanyNotificationsListHandler.getPscNotification(any())).thenReturn(privateCompanyNotification);
        when(privateCompanyNotification.execute()).thenThrow(uriValidationException);

        //when
        client.getNotification("/company/12345678/notifications/987ihg654fed321cba");

        //then
        verify(privateCompanyNotificationsListHandler).getPscNotification("/company/12345678/notifications/987ihg654fed321cba");
        verify(responseHandler).handle("Notifications API GET Notification", uriValidationException);
    }

    @Test
    void shouldFetchNotificationsList() throws ApiErrorResponseException, URIValidationException {
        //given
        when(apiClient.privatePscNotificationsListHandler()).thenReturn(notificationsListHandler);
        when(notificationsListHandler.getNotificationsList(any())).thenReturn(
                privatePscNotificationsListGet);
        when(privatePscNotificationsListGet.queryParams(any())).thenReturn(privatePscNotificationsListGet);
        when(privatePscNotificationsListGet.execute()).thenReturn(
                new ApiResponse<>(200, Collections.emptyMap(), notificationList));

        //when
        Optional<NotificationList> actual = client.getPscNotificationListForUpsert("/psc/abc123def456ghi789/notifications");

        //then
        assertTrue(actual.isPresent());
        assertEquals(notificationList, actual.get());
        verify(privatePscNotificationsListGet).queryParams(queryParamCaptor.capture());
        QueryParam queryParamArgument = queryParamCaptor.getValue().getFirst();
        assertEquals("items_per_page", queryParamArgument.getKey());
        assertEquals("500", queryParamArgument.getValue());
        verify(notificationsListHandler).getNotificationsList("/psc/abc123def456ghi789/notifications");
    }

    @Test
    void fetchNotificationListForUpsertApiErrorResponseException404NotFound()
        throws ApiErrorResponseException, URIValidationException {
        //given
        HttpResponseException.Builder builder = new HttpResponseException.Builder(404,
                "not found", new HttpHeaders());
        ApiErrorResponseException apiErrorResponseException = new ApiErrorResponseException(builder);

        when(apiClient.privatePscNotificationsListHandler()).thenReturn(notificationsListHandler);
        when(notificationsListHandler.getNotificationsList(any())).thenReturn(
                privatePscNotificationsListGet);
        when(privatePscNotificationsListGet.queryParams(any())).thenReturn(privatePscNotificationsListGet);
        when(privatePscNotificationsListGet.execute()).thenThrow(apiErrorResponseException);

        //when
        Optional<NotificationList> actual = client.getPscNotificationListForUpsert("/psc/abc123def456ghi789/notifications");

        //then
        assertTrue(actual.isEmpty());
        verify(privatePscNotificationsListGet).queryParams(queryParamCaptor.capture());
        QueryParam queryParamArgument = queryParamCaptor.getValue().getFirst();
        assertEquals("items_per_page", queryParamArgument.getKey());
        assertEquals("500", queryParamArgument.getValue());
        verify(notificationsListHandler).getNotificationsList("/psc/abc123def456ghi789/notifications");
        verifyNoInteractions(responseHandler);
    }

    @Test
    void fetchNotificationListForDeleteApiErrorResponseException404NotFoundAndLogError()
        throws ApiErrorResponseException, URIValidationException {
        //given
        HttpResponseException.Builder builder = new HttpResponseException.Builder(404,
                "not found", new HttpHeaders());
        ApiErrorResponseException apiErrorResponseException = new ApiErrorResponseException(
                builder);

        when(apiClient.privatePscNotificationsListHandler()).thenReturn(notificationsListHandler);
        when(notificationsListHandler.getNotificationsList(any())).thenReturn(
                privatePscNotificationsListGet);
        when(privatePscNotificationsListGet.queryParams(any())).thenReturn(privatePscNotificationsListGet);
        when(privatePscNotificationsListGet.execute()).thenThrow(apiErrorResponseException);

        //when
        Optional<NotificationList> actual = client.getPscNotificationListForDelete("/psc/abc123def456ghi789/notifications");

        //then
        assertTrue(actual.isEmpty());
        verify(privatePscNotificationsListGet).queryParams(queryParamCaptor.capture());
        QueryParam queryParamArgument = queryParamCaptor.getValue().getFirst();
        assertEquals("items_per_page", queryParamArgument.getKey());
        assertEquals("500", queryParamArgument.getValue());
        verify(notificationsListHandler).getNotificationsList("/psc/abc123def456ghi789/notifications");
        verifyNoInteractions(responseHandler);
    }

    @Test
    void fetchNotificationListApiErrorResponseException()
        throws ApiErrorResponseException, URIValidationException {
        //given
        HttpResponseException.Builder builder = new HttpResponseException.Builder(503,
                "service unavailable", new HttpHeaders());
        ApiErrorResponseException apiErrorResponseException = new ApiErrorResponseException(
                builder);

        when(apiClient.privatePscNotificationsListHandler()).thenReturn(notificationsListHandler);
        when(notificationsListHandler.getNotificationsList(any())).thenReturn(
                privatePscNotificationsListGet);
        when(privatePscNotificationsListGet.queryParams(any())).thenReturn(privatePscNotificationsListGet);
        when(privatePscNotificationsListGet.execute()).thenThrow(apiErrorResponseException);

        //when
        client.getPscNotificationListForDelete("/psc/abc123def456ghi789/notifications");

        //then
        verify(privatePscNotificationsListGet).queryParams(queryParamCaptor.capture());
        QueryParam queryParamArgument = queryParamCaptor.getValue().getFirst();
        assertEquals("items_per_page", queryParamArgument.getKey());
        assertEquals("500", queryParamArgument.getValue());
        verify(notificationsListHandler).getNotificationsList("/psc/abc123def456ghi789/notifications");
        verify(responseHandler).handle("Notifications API GET Psc Notifications", "/psc/abc123def456ghi789/notifications", apiErrorResponseException);
    }

    @Test
    void fetchNotificationListURIValidationException()
        throws ApiErrorResponseException, URIValidationException {
        //given
        URIValidationException uriValidationException = new URIValidationException("Invalid URI");
        when(apiClient.privatePscNotificationsListHandler()).thenReturn(notificationsListHandler);
        when(notificationsListHandler.getNotificationsList(any())).thenReturn(
                privatePscNotificationsListGet);
        when(privatePscNotificationsListGet.queryParams(any())).thenReturn(privatePscNotificationsListGet);
        when(privatePscNotificationsListGet.execute()).thenThrow(uriValidationException);

        //when
        client.getPscNotificationListForUpsert("/psc/abc123def456ghi789/notifications");

        //then
        verify(privatePscNotificationsListGet).queryParams(queryParamCaptor.capture());
        QueryParam queryParamArgument = queryParamCaptor.getValue().getFirst();
        assertEquals("items_per_page", queryParamArgument.getKey());
        assertEquals("500", queryParamArgument.getValue());
        verify(notificationsListHandler).getNotificationsList("/psc/abc123def456ghi789/notifications");
        verify(responseHandler).handle("Notifications API GET Psc Notifications", uriValidationException);
    }
}
