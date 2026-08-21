package uk.gov.companieshouse.pscmerge.service;

import java.util.Collections;

import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.api.psc.PscList;
import uk.gov.companieshouse.common.client.NotificationsApiClient;
import uk.gov.companieshouse.common.client.PrimarySearchApiClient;
import uk.gov.companieshouse.common.logging.DataMapHolder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.pscmerge.PscMerge;

@Component
public class PscMergeService implements MergeService {

    private static final Logger LOGGER = LoggerFactory.getLogger("psc-search-consumer");
    private static final String URI = "/psc/%s/notifications";

    private final NotificationsApiClient notificationsApiClient;
    private final PrimarySearchApiClient primarySearchApiClient;

    public PscMergeService(NotificationsApiClient notificationsApiClient, PrimarySearchApiClient primarySearchApiClient) {
        this.notificationsApiClient = notificationsApiClient;
        this.primarySearchApiClient = primarySearchApiClient;
    }

    @Override
    public void processMessage(Message<PscMerge> message) {
        PscMerge pscMerge = message.getPayload();
        final String previousPscId = pscMerge.getPreviousPscId();
        DataMapHolder.get()
                .pscId(pscMerge.getPscId())
                .previousPscId(previousPscId);
        notificationsApiClient.getPscNotificationListForDelete(URI.formatted(previousPscId))
                .ifPresentOrElse(notificationList -> {
                    LOGGER.info("Updating previous notification in index", DataMapHolder.getLogMap());
                    PscList pscList = new PscList();
                    pscList.setActiveCount(notificationList.getActiveCount());
                    pscList.setCeasedCount(notificationList.getCeasedCount());
                    pscList.setItemsPerPage(notificationList.getItemsPerPage());
                    pscList.setStartIndex(notificationList.getStartIndex());
                    pscList.setTotalResults(notificationList.getTotalResults());
                    pscList.setLinks(notificationList.getLinks());
                    pscList.setItems(Collections.emptyList());
                    primarySearchApiClient.upsertPsc(previousPscId, pscList);
                }, () -> {
                   LOGGER.info("Deleting previous psc from index", DataMapHolder.getLogMap());
                   primarySearchApiClient.deletePsc(previousPscId);
                });
    }
}
