package uk.gov.companieshouse.resourcechanged.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import uk.gov.companieshouse.api.psc_notifications.NotificationList;
import uk.gov.companieshouse.api.psc_notifications.PscNotificationSummary;
import uk.gov.companieshouse.common.client.NotificationsApiClient;
import uk.gov.companieshouse.common.client.PrimarySearchApiClient;
import uk.gov.companieshouse.common.exception.NonRetryableException;
import uk.gov.companieshouse.common.exception.PscDeserialisationException;
import uk.gov.companieshouse.resourcechanged.serdes.PscDeserialiser;
import uk.gov.companieshouse.stream.ResourceChangedData;
import uk.gov.companieshouse.resourcechanged.util.PscIdExtractor;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PscSearchUpsertServiceTest {

    private static final String DESTINATION_PSC_ID = "psc-a";
    private static final String PREVIOUS_PSC_ID = "psc-b";
    private static final String APPOINTMENT_2 = "appointment-2";
    private static final String APPOINTMENT_3 = "appointment-3";
    private static final String PSC_NOTIFICATIONS_URI = "/persons-with-significant-control/%s/notifications";

    @Mock
    private PscDeserialiser deserialiser;
    @Mock
    private PrimarySearchApiClient primarySearchApiClient;
    @Mock
    private PscNotificationSummary pscNotificationSummary;
    @Mock
    private ResourceChangedData resourceChangedData;
    @Mock
    private PscIdExtractor pscIdExtractor;
    @Mock
    private NotificationsApiClient notificationsApiClient;
    @InjectMocks
    private PscSearchUpsertService upsertService;

    @Test
    void shouldProcessMessage() {
        when(resourceChangedData.getData()).thenReturn("{\"some\":\"json\"}");
        when(resourceChangedData.getResourceId()).thenReturn(DESTINATION_PSC_ID);
        when(deserialiser.deserialisePscNotificationSummary(anyString())).thenReturn(pscNotificationSummary);
        when(pscIdExtractor.extractPscId(pscNotificationSummary)).thenReturn(Optional.of(DESTINATION_PSC_ID));
        NotificationList notificationList = mock(NotificationList.class);
        when(notificationsApiClient.getPscNotificationListForUpsert(PSC_NOTIFICATIONS_URI.formatted(DESTINATION_PSC_ID)))
            .thenReturn(Optional.of(notificationList));

        upsertService.processMessage(new ResourceChangedServiceParameters(resourceChangedData));

        ArgumentCaptor<NotificationList> captor = ArgumentCaptor.forClass(NotificationList.class);
        verify(primarySearchApiClient).upsertPsc(eq(DESTINATION_PSC_ID), captor.capture());
        assertSame(notificationList, captor.getValue());
        verify(notificationsApiClient).getPscNotificationListForUpsert(PSC_NOTIFICATIONS_URI.formatted(DESTINATION_PSC_ID));
    }

    @Test
    void shouldUpsertDestinationPscWhenOnlyAppointmentIsMerged() {
        NotificationList destinationNotifications = mock(NotificationList.class);

        processChangedAppointment(APPOINTMENT_2, DESTINATION_PSC_ID, destinationNotifications);

        verifyDestinationPscWasUpserted(destinationNotifications);
    }

    @Test
    void shouldUpsertDestinationPscWhenFirstAppointmentIsMerged() {
        NotificationList destinationNotifications = mock(NotificationList.class);

        processChangedAppointment(APPOINTMENT_2, DESTINATION_PSC_ID, destinationNotifications);

        verifyDestinationPscWasUpserted(destinationNotifications);
    }

    @Test
    void shouldUpsertDestinationPscWhenRemainingAppointmentIsMerged() {
        NotificationList destinationNotifications = mock(NotificationList.class);

        processChangedAppointment(APPOINTMENT_3, DESTINATION_PSC_ID, destinationNotifications);

        verifyDestinationPscWasUpserted(destinationNotifications);
    }

    @Test
    void shouldUpsertDestinationPscWhenPreviouslyMergedAppointmentIsUpdated() {
        NotificationList destinationNotifications = mock(NotificationList.class);

        processChangedAppointment(APPOINTMENT_2, DESTINATION_PSC_ID, destinationNotifications);

        verifyDestinationPscWasUpserted(destinationNotifications);
    }

    private void processChangedAppointment(String appointmentId, String destinationPscId,
                                           NotificationList destinationNotifications) {
        String data = "{\"appointment_id\":\"%s\",\"psc_id\":\"%s\",\"previous_psc_id\":\"%s\"}"
                .formatted(appointmentId, destinationPscId, PREVIOUS_PSC_ID);
        when(resourceChangedData.getData()).thenReturn(data);
        when(resourceChangedData.getResourceId()).thenReturn(appointmentId);
        when(deserialiser.deserialisePscNotificationSummary(data)).thenReturn(pscNotificationSummary);
        when(pscIdExtractor.extractPscId(pscNotificationSummary)).thenReturn(Optional.of(destinationPscId));
        when(notificationsApiClient.getPscNotificationListForUpsert(PSC_NOTIFICATIONS_URI.formatted(destinationPscId)))
                .thenReturn(Optional.of(destinationNotifications));

        upsertService.processMessage(new ResourceChangedServiceParameters(resourceChangedData));
    }

    private void verifyDestinationPscWasUpserted(NotificationList destinationNotifications) {
        ArgumentCaptor<NotificationList> captor = ArgumentCaptor.forClass(NotificationList.class);
        verify(primarySearchApiClient).upsertPsc(eq(DESTINATION_PSC_ID), captor.capture());
        assertSame(destinationNotifications, captor.getValue());
        verify(notificationsApiClient).getPscNotificationListForUpsert(PSC_NOTIFICATIONS_URI.formatted(DESTINATION_PSC_ID));
    }

    @Test
    void shouldThrowExceptionWhenDeserialisationFails() {
        when(resourceChangedData.getData()).thenReturn("invalid-data");
        when(deserialiser.deserialisePscNotificationSummary(anyString())).thenThrow(new PscDeserialisationException("fail", new RuntimeException("bad json")));
        ResourceChangedServiceParameters params = new ResourceChangedServiceParameters(resourceChangedData);

        Executable executable = () -> upsertService.processMessage(params);
        PscDeserialisationException exception = assertThrows(PscDeserialisationException.class, executable);
        assertEquals("fail", exception.getMessage());
        verifyNoInteractions(primarySearchApiClient);
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenPscIdCannotBeExtracted() {
        when(resourceChangedData.getData()).thenReturn("appointment-data");
        when(resourceChangedData.getResourceId()).thenReturn(APPOINTMENT_2);
        when(deserialiser.deserialisePscNotificationSummary(anyString())).thenReturn(pscNotificationSummary);
        when(pscIdExtractor.extractPscId(pscNotificationSummary)).thenReturn(Optional.empty());
        ResourceChangedServiceParameters params = new ResourceChangedServiceParameters(resourceChangedData);

        assertThrows(NonRetryableException.class, () -> upsertService.processMessage(params));
        verifyNoInteractions(primarySearchApiClient);
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenNotificationsUnavailable() {
        when(resourceChangedData.getData()).thenReturn("appointment-data");
        when(resourceChangedData.getResourceId()).thenReturn(APPOINTMENT_2);
        when(deserialiser.deserialisePscNotificationSummary(anyString())).thenReturn(pscNotificationSummary);
    when(pscIdExtractor.extractPscId(pscNotificationSummary)).thenReturn(Optional.of(DESTINATION_PSC_ID));
    when(notificationsApiClient.getPscNotificationListForUpsert(PSC_NOTIFICATIONS_URI.formatted(DESTINATION_PSC_ID))).thenReturn(Optional.empty());
        ResourceChangedServiceParameters params = new ResourceChangedServiceParameters(resourceChangedData);

        NonRetryableException exception = assertThrows(NonRetryableException.class,
                () -> upsertService.processMessage(params));
        assertEquals("PSC notifications unavailable", exception.getMessage());
        verify(primarySearchApiClient, never()).upsertPsc(anyString(), any());
    }

}


