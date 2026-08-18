package uk.gov.companieshouse.pscmerge.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import uk.gov.companieshouse.api.psc.PscList;
import uk.gov.companieshouse.api.psc_notifications.NotificationList;
import uk.gov.companieshouse.common.client.NotificationsApiClient;
import uk.gov.companieshouse.common.client.PrimarySearchApiClient;
import uk.gov.companieshouse.pscmerge.PscMerge;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PscMergeServiceTest {

    private static final String PSC_NOTIFICATIONS_LINK_MERGE = "/psc/previous_psc_id/notifications";
    private static final String PREVIOUS_PSC_ID = "previous_psc_id";
    private static final String PSC_ID = "psc_id";
    private static final String CONTEXT_ID = "context_id";

    @Mock
    private Message<PscMerge> pscMergeMessage;
    @Mock
    private PrimarySearchApiClient searchClient;
    @Mock
    private NotificationsApiClient notificationsApiClient;
    @Mock
    private NotificationList notificationList;
    @Mock
    private PscList pscList;

    @InjectMocks
    private PscMergeService pscMergeService;

    @Test
    void shouldUpsertOfficerAppointmentsToPrimarySearchApiIfAnyAppointmentsFoundForPreviousPscId() {
        //given
        when(pscMergeMessage.getPayload()).thenReturn(PSC_MERGE_MESSAGE_PAYLOAD);
        when(notificationsApiClient.getPscNotificationListForDelete(anyString())).thenReturn(Optional.of(notificationList));

        //when
        pscMergeService.processMessage(pscMergeMessage);

        //then
        verify(notificationsApiClient).getPscNotificationListForDelete(PSC_NOTIFICATIONS_LINK_MERGE);
        verify(searchClient).upsertPsc(eq(PREVIOUS_PSC_ID), any(PscList.class));
    }

    @Test
    void shouldDeletePscNotificationsIfNoNotificationsFoundForPreviousPscId() {
        //given
        when(pscMergeMessage.getPayload()).thenReturn(PSC_MERGE_MESSAGE_PAYLOAD);
        when(notificationsApiClient.getPscNotificationListForDelete(anyString())).thenReturn(Optional.empty());

        //when
        pscMergeService.processMessage(pscMergeMessage);

        //then
        verify(notificationsApiClient).getPscNotificationListForDelete(PSC_NOTIFICATIONS_LINK_MERGE);
        verify(searchClient).deletePsc(PREVIOUS_PSC_ID);
    }

    private static final PscMerge PSC_MERGE_MESSAGE_PAYLOAD = PscMerge.newBuilder()
            .setPscId(PSC_ID)
            .setContextId(CONTEXT_ID)
            .setPreviousPscId(PREVIOUS_PSC_ID)
            .build();
}
