package uk.gov.companieshouse.resourcechanged.service;

import org.springframework.stereotype.Component;
    import uk.gov.companieshouse.api.psc_notifications.PscNotificationSummary;
import uk.gov.companieshouse.common.client.NotificationsApiClient;
import uk.gov.companieshouse.common.client.PrimarySearchApiClient;
import uk.gov.companieshouse.common.exception.NonRetryableException;
import uk.gov.companieshouse.common.exception.RetryableException;
import uk.gov.companieshouse.common.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.resourcechanged.serdes.PscDeserialiser;
import uk.gov.companieshouse.resourcechanged.util.NotificationsLinkInfo;
import uk.gov.companieshouse.resourcechanged.util.PscLinkInfoExtractor;
import uk.gov.companieshouse.stream.ResourceChangedData;

import static uk.gov.companieshouse.Application.NAMESPACE;

@Component
public class PscSearchDeleteService implements ResourceChangedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final PrimarySearchApiClient apiClientService;
    private final PscLinkInfoExtractor pscLinkInfoExtractor;
    private final PscDeserialiser deserialiser;
    private final NotificationsApiClient notificationsApiClient;

    public PscSearchDeleteService(PrimarySearchApiClient apiClientService, PscLinkInfoExtractor pscLinkInfoExtractor,
                                  PscDeserialiser deserialiser, NotificationsApiClient notificationsApiClient) {
        this.apiClientService = apiClientService;
        this.pscLinkInfoExtractor = pscLinkInfoExtractor;
        this.deserialiser = deserialiser;
        this.notificationsApiClient = notificationsApiClient;
    }

    @Override
    public void processMessage(ResourceChangedServiceParameters parameters) {

        ResourceChangedData payload = parameters.getData();

        PscNotificationSummary pscNotificationSummary = deserialiser.deserialisePscNotificationSummary(payload.getData());
        NotificationsLinkInfo notificationsLinkInfo = pscLinkInfoExtractor.extractNotificationsLinkInfo(pscNotificationSummary)
                .orElseThrow(() -> {
                    LOGGER.error("Could not extract PSC ID from notifications link for delete request, resourceId: " + payload.getResourceId());
                    return new NonRetryableException(
                            "Could not extract PSC ID from notifications link for delete request, resourceId: " + payload.getResourceId());
                });
        DataMapHolder.get().pscId(notificationsLinkInfo.pscId());

        notificationsApiClient.getNotification(notificationsLinkInfo.notificationsLink())
                .ifPresent(notification -> {
                    throw new RetryableException("PSC has not yet been deleted");
                });
        notificationsApiClient.getPscNotificationListForDelete(notificationsLinkInfo.notificationsLink())
                .ifPresentOrElse(notificationList -> apiClientService.upsertPsc(notificationsLinkInfo.pscId(), notificationList),
                        () -> apiClientService.deletePsc(notificationsLinkInfo.pscId()));
    }
}
