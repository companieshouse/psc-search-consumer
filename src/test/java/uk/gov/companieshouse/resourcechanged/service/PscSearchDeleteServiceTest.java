package uk.gov.companieshouse.resourcechanged.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.psc.ListSummary;
import uk.gov.companieshouse.common.client.PrimarySearchApiClient;
import uk.gov.companieshouse.common.exception.NonRetryableException;
import uk.gov.companieshouse.resourcechanged.serdes.PscDeserialiser;
import uk.gov.companieshouse.resourcechanged.util.PscIdExtractor;
import uk.gov.companieshouse.stream.ResourceChangedData;

import java.util.Optional;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
	private PscIdExtractor pscIdExtractor;

	@Test
	void processMessageCallsApi() {
		// given
		PscSearchDeleteService service = new PscSearchDeleteService(apiClient, pscIdExtractor, deserialiser);
		String pscId = "pscid-123";

		ResourceChangedData data = mock(ResourceChangedData.class);
		ListSummary listSummary = mock(ListSummary.class);

		when(data.getResourceId()).thenReturn("resource-id");
		when(data.getData()).thenReturn("some-payload");
		when(deserialiser.deserialiseListSummary(anyString())).thenReturn(listSummary);

		// extractor is called with the deserialised ListSummary
		when(pscIdExtractor.extractPscId(listSummary)).thenReturn(Optional.of(pscId));


		ResourceChangedServiceParameters params = new ResourceChangedServiceParameters(data);

		// when
		service.processMessage(params);

		// then - api called
		verify(apiClient).deletePsc(pscId);
	}


	@Test
	void shouldThrowNonRetryableExceptionWhenPscIdCannotBeExtracted() {
		// given
		PscSearchDeleteService service = new PscSearchDeleteService(apiClient, pscIdExtractor, deserialiser);

		ResourceChangedData data = mock(ResourceChangedData.class);
		ListSummary listSummary = mock(ListSummary.class);

		when(data.getData()).thenReturn("some-payload");
		when(deserialiser.deserialiseListSummary(anyString())).thenReturn(listSummary);

		// extractor cannot extract PSC id
		when(pscIdExtractor.extractPscId(listSummary)).thenReturn(Optional.empty());
		when(data.getResourceId()).thenReturn("resource-id");

		ResourceChangedServiceParameters params = new ResourceChangedServiceParameters(data);

		// then - NonRetryableException is thrown
		assertThrows(NonRetryableException.class, () -> service.processMessage(params));

		// and API not called
		verify(apiClient, never()).deletePsc(anyString());
	}
}
