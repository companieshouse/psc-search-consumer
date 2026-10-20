package uk.gov.companieshouse.resourcechanged.service;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.companieshouse.api.psc_notifications.PscNotificationSummary;
import uk.gov.companieshouse.common.client.NotificationsApiClient;
import uk.gov.companieshouse.common.exception.NonRetryableException;
import uk.gov.companieshouse.common.logging.DataMapHolder;
import uk.gov.companieshouse.resourcechanged.serdes.PscDeserialiser;
import uk.gov.companieshouse.resourcechanged.util.NotificationsLinkInfo;
import uk.gov.companieshouse.resourcechanged.util.PscLinkInfoExtractor;
import uk.gov.companieshouse.stream.ResourceChangedData;
import uk.gov.companieshouse.common.client.PrimarySearchApiClient;

@Component
public class PscSearchUpsertService implements ResourceChangedService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PscSearchUpsertService.class);
    private final PscDeserialiser deserialiser;
    private final PrimarySearchApiClient primarySearchApiClient;
    private final PscLinkInfoExtractor pscLinkInfoExtractor;
    private final NotificationsApiClient notificationsApiClient;

    public PscSearchUpsertService(PscDeserialiser deserialiser, PrimarySearchApiClient primarySearchApiClient,
                                  PscLinkInfoExtractor pscLinkInfoExtractor, NotificationsApiClient notificationsApiClient) {
        this.deserialiser = deserialiser;
        this.primarySearchApiClient = primarySearchApiClient;
        this.pscLinkInfoExtractor = pscLinkInfoExtractor;
        this.notificationsApiClient = notificationsApiClient;
    }

    @Override
    public void processMessage(ResourceChangedServiceParameters parameters) {

        ResourceChangedData payload = parameters.getData();
        PscNotificationSummary pscNotificationSummary = deserialiser.deserialisePscNotificationSummary(payload.getData());
        String resourceId = payload.getResourceId();
        NotificationsLinkInfo notificationsLinkInfo = pscLinkInfoExtractor.extractNotificationsLinkInfo(pscNotificationSummary)
                .orElseThrow(() -> {
                    LOGGER.error("Could not extract PSC ID from notifications link, resourceId: {}", resourceId);
                    return new NonRetryableException(
                            "Could not extract PSC ID from notifications link for resourceId: " + resourceId);
                });

        DataMapHolder.get().pscId(notificationsLinkInfo.pscId());
        notificationsApiClient.getPscNotificationListForUpsert(notificationsLinkInfo.notificationsLink())
                .ifPresentOrElse(notificationList -> primarySearchApiClient.upsertPsc(notificationsLinkInfo.pscId(), notificationList),
                        () -> {
                            LOGGER.error("PSC notifications unavailable. {}", DataMapHolder.getLogMap());
                            throw new NonRetryableException("PSC notifications unavailable");
                        });
    }
}
