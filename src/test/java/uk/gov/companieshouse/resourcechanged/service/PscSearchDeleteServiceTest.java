package uk.gov.companieshouse.resourcechanged.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.common.client.PrimarySearchApiClient;
import uk.gov.companieshouse.stream.ResourceChangedData;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PscSearchDeleteServiceTest {

	@Mock
	private PrimarySearchApiClient apiClient;

	@Test
	void processMessage_callsApi() {
		// given
		PscSearchDeleteService service = new PscSearchDeleteService(apiClient);
		String resourceId = "resource-123";

		ResourceChangedData data = mock(ResourceChangedData.class);
		when(data.getResourceId()).thenReturn(resourceId);

		ResourceChangedServiceParameters params = new ResourceChangedServiceParameters(data);

		// when
		service.processMessage(params);

		// then - api called
		verify(apiClient).deletePsc(resourceId);
	}
}
