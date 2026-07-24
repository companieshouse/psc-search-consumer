package uk.gov.companieshouse.common.client;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.officer.AppointmentList;
import uk.gov.companieshouse.api.psc_notifications.PscNotificationSummary;
import uk.gov.companieshouse.api.request.QueryParam;
import uk.gov.companieshouse.common.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class NotificationsApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger("psc-search-consumer");
    private static final String GET_NOTIFICATION_CALL = "Notifications API GET Notification";
    private static final String GET_PSC_NOTIFICATIONS_CALL = "Notifications API GET Psc Notifications";
    private static final List<QueryParam> ITEMS_PER_PAGE_500 = List.of(new QueryParam("items_per_page", "500"));

    private final Supplier<InternalApiClient> apiClientSupplier;
    private final ResponseHandler responseHandler;

    NotificationsApiClient(Supplier<InternalApiClient> apiClientSupplier, ResponseHandler responseHandler) {
        this.apiClientSupplier = apiClientSupplier;
        this.responseHandler = responseHandler;
    }

    public Optional<PscNotificationSummary> getNotification(String resourceUri) {
        InternalApiClient apiClient = apiClientSupplier.get();
        apiClient.getHttpClient().setRequestId(DataMapHolder.getRequestId());
        try {
            return Optional.of(apiClient.privateCompanyAppointmentsListHandler()
                    .getCompanyAppointment(resourceUri)
                    .execute()
                    .getData());
        } catch (ApiErrorResponseException ex) {
            if (ex.getStatusCode() == 404) {
                return Optional.empty();
            } else {
                responseHandler.handle(GET_NOTIFICATION_CALL, resourceUri, ex);
            }
        } catch (URIValidationException ex) {
            responseHandler.handle(GET_NOTIFICATION_CALL, ex);
        }
        return Optional.empty();
    }

    public Optional<AppointmentList> getPscAppointmentListForDelete(String resourceUri) {
        return getPscAppointmentList(resourceUri, false);
    }

    public Optional<AppointmentList> getPscAppointmentListForUpsert(String resourceUri) {
        return getPscAppointmentList(resourceUri, true);
    }

    public Optional<AppointmentList> getPscAppointmentList(String resourceUri, boolean isUpsert) {

        InternalApiClient apiClient = apiClientSupplier.get();
        apiClient.getHttpClient().setRequestId(DataMapHolder.getRequestId());
        try {
            return Optional.of(apiClient.privateOfficerAppointmentsListHandler()
                    .getAppointmentsList(resourceUri)
                    .queryParams(ITEMS_PER_PAGE_500)
                    .execute()
                    .getData());
        } catch (ApiErrorResponseException ex) {
            if (ex.getStatusCode() == 404) {
                if (isUpsert) {
                    LOGGER.error(ex.getMessage(), ex, DataMapHolder.getLogMap());
                }
                return Optional.empty();
            } else {
                responseHandler.handle(GET_PSC_NOTIFICATIONS_CALL, resourceUri, ex);
            }
        } catch (URIValidationException ex) {
            responseHandler.handle(GET_PSC_NOTIFICATIONS_CALL, ex);
        }

        return Optional.empty();
    }

}
