package uk.gov.companieshouse.pscmerge.service;

import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
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
                .previousOfficerId(previousPscId);
        notificationsApiClient.getPscAppointmentListForDelete(URI.formatted(previousPscId))
                .ifPresentOrElse(pscList -> {
                    LOGGER.info("Updating previous notification in index", DataMapHolder.getLogMap());
                    primarySearchApiClient.upsertPsc(previousPscId, pscList);
                }, () -> {
                   LOGGER.info("Deleting previous officer from index", DataMapHolder.getLogMap());
                   primarySearchApiClient.deletePsc(previousPscId);
                });
    }
}
