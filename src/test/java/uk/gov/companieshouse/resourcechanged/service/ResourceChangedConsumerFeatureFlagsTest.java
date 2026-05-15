package uk.gov.companieshouse.resourcechanged.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import uk.gov.companieshouse.resourcechanged.config.ResourceChangedConfig;
import uk.gov.companieshouse.resourcechanged.ResourceChangedConsumer;
import uk.gov.companieshouse.stream.ResourceChangedData;
import uk.gov.companieshouse.common.exception.MessageFlags;

class ResourceChangedConsumerFeatureFlagsTest {

    @Test
    void doesNotProcessMessagesWhenFlagDisabled() {
        PscSearchUpdaterServiceRouter pscSearchUpdaterServiceRouter = mock(PscSearchUpdaterServiceRouter.class);
        MessageFlags messageFlags = mock(MessageFlags.class);
        ResourceChangedConfig resourceChangedConfig = mock(ResourceChangedConfig.class);
        when(resourceChangedConfig.isPscConsumerEnabled()).thenReturn(false);

        ResourceChangedConsumer resourceChangedConsumer = new ResourceChangedConsumer(pscSearchUpdaterServiceRouter, messageFlags, resourceChangedConfig);

        Message<ResourceChangedData> message = mock(Message.class);
        when(message.getPayload()).thenReturn(new ResourceChangedData());

        resourceChangedConsumer.consume(message);

        verify(pscSearchUpdaterServiceRouter, never()).route(any());
    }

    @Test
    void processesMessagesWhenFlagEnabled() {
        PscSearchUpdaterServiceRouter pscSearchUpdaterServiceRouter = mock(PscSearchUpdaterServiceRouter.class);
        MessageFlags messageFlags = mock(MessageFlags.class);
        ResourceChangedConfig resourceChangedConfig = mock(ResourceChangedConfig.class);
        when(resourceChangedConfig.isPscConsumerEnabled()).thenReturn(true);

        ResourceChangedConsumer resourceChangedConsumer = new ResourceChangedConsumer(pscSearchUpdaterServiceRouter, messageFlags, resourceChangedConfig);

        Message<ResourceChangedData> message = mock(Message.class);
        when(message.getPayload()).thenReturn(new ResourceChangedData());

        resourceChangedConsumer.consume(message);

        verify(pscSearchUpdaterServiceRouter, times(1)).route(any());
    }
    
}
