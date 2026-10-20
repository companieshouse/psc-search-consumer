package uk.gov.companieshouse.resourcechanged.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.psc_notifications.PscNotificationSummary;
import uk.gov.companieshouse.common.client.NotificationsApiClient;
import uk.gov.companieshouse.common.client.PrimarySearchApiClient;
import uk.gov.companieshouse.common.exception.NonRetryableException;
import uk.gov.companieshouse.common.exception.RetryableException;
import uk.gov.companieshouse.resourcechanged.serdes.PscDeserialiser;
import uk.gov.companieshouse.resourcechanged.util.NotificationsLinkInfo;
import uk.gov.companieshouse.resourcechanged.util.PscLinkInfoExtractor;
import uk.gov.companieshouse.stream.ResourceChangedData;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PscSearchDeleteServiceTest {

	@Mock
	private PrimarySearchApiClient apiClient;
	@Mock
	private PscDeserialiser deserialiser;
	@Mock
	private PscLinkInfoExtractor pscLinkInfoExtractor;
	@Mock
	private NotificationsApiClient notificationsApiClient;

	@Test
	void processMessageCallsApi() {
		// given
		PscSearchDeleteService service = new PscSearchDeleteService(apiClient, pscLinkInfoExtractor, deserialiser, notificationsApiClient);
		NotificationsLinkInfo notificationsLinkInfo = new NotificationsLinkInfo("pscid-123", "/persons-with-significant-control/pscid-123/notifications");

		ResourceChangedData data = mock(ResourceChangedData.class);
		PscNotificationSummary pscNotificationSummary = mock(PscNotificationSummary.class);

		when(data.getData()).thenReturn("some-payload");
		when(notificationsApiClient.getNotification(notificationsLinkInfo.notificationsLink())).thenReturn(Optional.empty());
		when(deserialiser.deserialisePscNotificationSummary(anyString())).thenReturn(pscNotificationSummary);

		when(pscLinkInfoExtractor.extractNotificationsLinkInfo(pscNotificationSummary)).thenReturn(Optional.of(notificationsLinkInfo));


		ResourceChangedServiceParameters params = new ResourceChangedServiceParameters(data);

		// when
		service.processMessage(params);

		// then - api called
		verify(apiClient).deletePsc(notificationsLinkInfo.pscId());
	}


	@Test
	void shouldThrowRetryableExceptionWhenNotificationIsPresent() {
		// given
		PscSearchDeleteService service = new PscSearchDeleteService(apiClient, pscLinkInfoExtractor, deserialiser, notificationsApiClient);
		NotificationsLinkInfo notificationsLinkInfo = new NotificationsLinkInfo("pscid-123", "/persons-with-significant-control/pscid-123/notifications");

		ResourceChangedData data = mock(ResourceChangedData.class);
		PscNotificationSummary notificationSummary = mock(PscNotificationSummary.class);

		when(data.getData()).thenReturn("some-payload");
		when(deserialiser.deserialisePscNotificationSummary(anyString())).thenReturn(notificationSummary);
		when(notificationsApiClient.getNotification(notificationsLinkInfo.notificationsLink())).thenReturn(Optional.of(notificationSummary));

		when(pscLinkInfoExtractor.extractNotificationsLinkInfo(notificationSummary)).thenReturn(Optional.of(notificationsLinkInfo));

		ResourceChangedServiceParameters params = new ResourceChangedServiceParameters(data);

		// then - RetryableException is thrown and processing stops
		assertThrows(RetryableException.class, () -> service.processMessage(params));
		verify(apiClient, never()).deletePsc(anyString());
	}

	@Test
	void shouldThrowNonRetryableExceptionWhenPscIdCannotBeExtracted() {
		// given
		PscSearchDeleteService service = new PscSearchDeleteService(apiClient, pscLinkInfoExtractor, deserialiser, notificationsApiClient);

		ResourceChangedData data = mock(ResourceChangedData.class);
		PscNotificationSummary pscNotificationSummary = mock(PscNotificationSummary.class);

		when(data.getData()).thenReturn("some-payload");
		when(deserialiser.deserialisePscNotificationSummary(anyString())).thenReturn(pscNotificationSummary);

		when(pscLinkInfoExtractor.extractNotificationsLinkInfo(pscNotificationSummary)).thenReturn(Optional.empty());
		when(data.getResourceId()).thenReturn("resource-id");

		ResourceChangedServiceParameters params = new ResourceChangedServiceParameters(data);

		// then - NonRetryableException is thrown
		assertThrows(NonRetryableException.class, () -> service.processMessage(params));

		// and API not called
		verify(apiClient, never()).deletePsc(anyString());
	}
}
