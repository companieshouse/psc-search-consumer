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
import uk.gov.companieshouse.resourcechanged.util.PscIdExtractor;
import uk.gov.companieshouse.stream.ResourceChangedData;

import static uk.gov.companieshouse.Application.NAMESPACE;

@Component
public class PscSearchDeleteService implements ResourceChangedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private final PrimarySearchApiClient apiClientService;
    private final PscIdExtractor pscIdExtractor;
    private final PscDeserialiser deserialiser;
    private final NotificationsApiClient notificationsApiClient;

    public PscSearchDeleteService(PrimarySearchApiClient apiClientService, PscIdExtractor pscIdExtractor,
                                  PscDeserialiser deserialiser, NotificationsApiClient notificationsApiClient) {
        this.apiClientService = apiClientService;
        this.pscIdExtractor = pscIdExtractor;
        this.deserialiser = deserialiser;
        this.notificationsApiClient = notificationsApiClient;
    }

    @Override
    public void processMessage(ResourceChangedServiceParameters parameters) {

        ResourceChangedData payload = parameters.getData();

        notificationsApiClient.getNotification(payload.getResourceUri())
                .ifPresent(pscNotificationSummary -> {
                    throw new RetryableException("PSC has not yet been deleted");
                });

        PscNotificationSummary pscNotificationSummary = deserialiser.deserialisePscNotificationSummary(payload.getData());
        String pscId = pscIdExtractor.extractPscId(pscNotificationSummary)
                .orElseThrow(() -> {
                    LOGGER.error("Could not extract PSC ID from notifications link for delete request, resourceId: " + payload.getResourceId());
                    return new NonRetryableException(
                            "Could not extract PSC ID from notifications link for delete request, resourceId: " + payload.getResourceId());
                });
        DataMapHolder.get().pscId(pscId);

        notificationsApiClient.getPscNotificationListForDelete(payload.getResourceUri())
                .ifPresentOrElse(notificationList -> apiClientService.upsertPsc(pscId, notificationList),
                        () -> apiClientService.deletePsc(pscId));
    }
}
