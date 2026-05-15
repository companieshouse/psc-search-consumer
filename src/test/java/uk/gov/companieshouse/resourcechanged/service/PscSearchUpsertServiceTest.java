package uk.gov.companieshouse.resourcechanged.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import uk.gov.companieshouse.api.psc.ListSummary;
import uk.gov.companieshouse.api.psc.PscList;
import uk.gov.companieshouse.common.client.PrimarySearchApiClient;
import uk.gov.companieshouse.common.exception.PscDeserialisationException;
import uk.gov.companieshouse.resourcechanged.serdes.PscDeserialiser;
import uk.gov.companieshouse.stream.ResourceChangedData;

@ExtendWith(MockitoExtension.class)
class PscSearchUpsertServiceTest {

    private static final String PSC_ID = "pscid_123";
    private static final String DATA = "{\"some\":\"json\"}";

    @Mock
    private PscDeserialiser deserialiser;
    @Mock
    private PrimarySearchApiClient primarySearchApiClient;
    @Mock
    private IdExtractor idExtractor;
    @Mock
    private ListSummary listSummary;
    @Mock
    private ResourceChangedData resourceChangedData;

    @InjectMocks
    private PscSearchUpsertService upsertService;

    @Test
    void shouldProcessMessage() {
        when(resourceChangedData.getData()).thenReturn(DATA);
        when(deserialiser.deserialiseListSummary(anyString())).thenReturn(listSummary);
        when(idExtractor.extractPscId(any())).thenReturn(PSC_ID);
        when(listSummary.getCeased()).thenReturn(false);
        when(listSummary.getLinks()).thenReturn(null);

        ResourceChangedServiceParameters params = new ResourceChangedServiceParameters(resourceChangedData);

        upsertService.processMessage(params);

        ArgumentCaptor<PscList> captor = ArgumentCaptor.forClass(PscList.class);
        verify(primarySearchApiClient).upsertPsc(eq(PSC_ID), captor.capture());
        PscList captured = captor.getValue();
        assertNotNull(captured);
        assertEquals(1, captured.getItems().size());
        assertEquals(1, captured.getItemsPerPage());
        assertEquals(0, captured.getStartIndex());
        assertEquals(1, captured.getTotalResults());
        assertEquals(1, captured.getActiveCount());
        assertEquals(0, captured.getCeasedCount());
    }

    @Test
    void shouldThrowExceptionWhenDeserialisationFails() {
        when(resourceChangedData.getData()).thenReturn(DATA);
        when(deserialiser.deserialiseListSummary(anyString())).thenThrow(new PscDeserialisationException("fail", new RuntimeException("bad json")));
        ResourceChangedServiceParameters params = new ResourceChangedServiceParameters(resourceChangedData);

        Executable executable = () -> upsertService.processMessage(params);
        PscDeserialisationException exception = assertThrows(PscDeserialisationException.class, executable);
        assertEquals("fail", exception.getMessage());
        verifyNoInteractions(primarySearchApiClient);
    }

}
