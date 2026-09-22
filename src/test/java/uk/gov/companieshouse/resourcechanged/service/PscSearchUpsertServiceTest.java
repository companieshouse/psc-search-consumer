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

    private static final String PSC_ID = "123";
    private static final String DATA = "{\"some\":\"json\"}";

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
        when(resourceChangedData.getData()).thenReturn(DATA);
        when(resourceChangedData.getResourceId()).thenReturn(PSC_ID);
        when(deserialiser.deserialisePscNotificationSummary(anyString())).thenReturn(pscNotificationSummary);
        when(pscIdExtractor.extractPscId(pscNotificationSummary)).thenReturn(Optional.of(PSC_ID));
        NotificationList notificationList = mock(NotificationList.class);
        when(notificationsApiClient.getPscNotificationListForUpsert(PSC_ID)).thenReturn(Optional.of(notificationList));
        ResourceChangedServiceParameters params = new ResourceChangedServiceParameters(resourceChangedData);

        upsertService.processMessage(params);

        ArgumentCaptor<NotificationList> captor = ArgumentCaptor.forClass(NotificationList.class);
        verify(primarySearchApiClient).upsertPsc(eq(PSC_ID), captor.capture());
        NotificationList captured = captor.getValue();
        assertNotNull(captured);
        assertSame(notificationList, captured);
        verify(notificationsApiClient).getPscNotificationListForUpsert(PSC_ID);
    }

    @Test
    void shouldThrowExceptionWhenDeserialisationFails() {
        when(resourceChangedData.getData()).thenReturn(DATA);
        when(deserialiser.deserialisePscNotificationSummary(anyString())).thenThrow(new PscDeserialisationException("fail", new RuntimeException("bad json")));
        ResourceChangedServiceParameters params = new ResourceChangedServiceParameters(resourceChangedData);

        Executable executable = () -> upsertService.processMessage(params);
        PscDeserialisationException exception = assertThrows(PscDeserialisationException.class, executable);
        assertEquals("fail", exception.getMessage());
        verifyNoInteractions(primarySearchApiClient);
    }

    @Test
    void shouldThrowNonRetryableExceptionWhenPscIdCannotBeExtracted() {
        when(resourceChangedData.getData()).thenReturn(DATA);
        when(resourceChangedData.getResourceId()).thenReturn(PSC_ID);
        when(deserialiser.deserialisePscNotificationSummary(anyString())).thenReturn(pscNotificationSummary);
        when(pscIdExtractor.extractPscId(pscNotificationSummary)).thenReturn(Optional.empty());
        ResourceChangedServiceParameters params = new ResourceChangedServiceParameters(resourceChangedData);

        assertThrows(NonRetryableException.class, () -> upsertService.processMessage(params));
        verifyNoInteractions(primarySearchApiClient);
    }

}


