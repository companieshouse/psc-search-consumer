package uk.gov.companieshouse.resourcechanged.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.common.exception.NonRetryableException;
import uk.gov.companieshouse.stream.EventRecord;
import uk.gov.companieshouse.stream.ResourceChangedData;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PscSearchUpdaterServiceRouterTest {

    @Mock
    private PscSearchDeleteService deleteService;

    @Mock
    private ResourceChangedUpsertService upsertService;

    @Test
    void routeUnknownMessageTypeThrowsNonRetryable() {
        PscSearchUpdaterServiceRouter router = new PscSearchUpdaterServiceRouter(deleteService, upsertService);

        ResourceChangedData data = mock(ResourceChangedData.class);
        when(data.getResourceId()).thenReturn("resource-unknown");
        when(data.getResourceKind()).thenReturn("company-psc-individual");
        when(data.getResourceUri()).thenReturn("/company/123/persons-with-significant-control/abc");

        // return an EventRecord with an unknown type
        when(data.getEvent()).thenReturn(EventRecord.newBuilder()
                .setPublishedAt("1453896193333")
                .setType("mystery-type")
                .setFieldsChanged(Collections.emptyList())
                .build());

        ResourceChangedServiceParameters params = new ResourceChangedServiceParameters(data);

        assertThrows(NonRetryableException.class, () -> router.route(params));
        verify(deleteService, never()).processMessage(params);
    }

    @Test
    void routeDeletedMessageCallsDeleteService() {
        PscSearchUpdaterServiceRouter router = new PscSearchUpdaterServiceRouter(deleteService, upsertService);

        ResourceChangedData data = mock(ResourceChangedData.class);
        when(data.getResourceId()).thenReturn("resource-123");
        when(data.getResourceKind()).thenReturn("company-psc-individual");
        when(data.getResourceUri()).thenReturn("/company/123/persons-with-significant-control/abc");

        when(data.getEvent()).thenReturn(EventRecord.newBuilder()
                .setPublishedAt("1453896193333")
                .setType("deleted")
                .setFieldsChanged(Collections.emptyList())
                .build());

        ResourceChangedServiceParameters params = new ResourceChangedServiceParameters(data);

        router.route(params);

        verify(deleteService).processMessage(params);
    }
}

