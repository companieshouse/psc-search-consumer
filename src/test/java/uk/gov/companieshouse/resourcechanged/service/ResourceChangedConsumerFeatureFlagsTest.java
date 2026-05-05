package uk.gov.companieshouse.resourcechanged.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import uk.gov.companieshouse.resourcechanged.service.ResourceChangedService;
import uk.gov.companieshouse.resourcechanged.config.ResourceChangedConfig;
import uk.gov.companieshouse.resourcechanged.ResourceChangedConsumer;
import uk.gov.companieshouse.stream.ResourceChangedData;
import uk.gov.companieshouse.common.exception.MessageFlags;

class ResourceChangedConsumerFeatureFlagsTest {

    @Test
    void doesNotProcessMessagesWhenFlagDisabled() {
        ResourceChangedService resourceChangedService = mock(ResourceChangedService.class);
        MessageFlags messageFlags = mock(MessageFlags.class);
        ResourceChangedConfig resourceChangedConfig = mock(ResourceChangedConfig.class);
        when(resourceChangedConfig.isPscConsumerEnabled()).thenReturn(false);

        ResourceChangedConsumer resourceChangedConsumer = new ResourceChangedConsumer(resourceChangedService, messageFlags, resourceChangedConfig);

        Message<ResourceChangedData> message = mock(Message.class);
        when(message.getPayload()).thenReturn(new ResourceChangedData());

        resourceChangedConsumer.consume(message);

        verify(resourceChangedService, never()).processMessage(any());
    }

    @Test
    void processesMessagesWhenFlagEnabled() {
        ResourceChangedService resourceChangedService = mock(ResourceChangedService.class);
        MessageFlags messageFlags = mock(MessageFlags.class);
        ResourceChangedConfig resourceChangedConfig = mock(ResourceChangedConfig.class);
        when(resourceChangedConfig.isPscConsumerEnabled()).thenReturn(true);

        ResourceChangedConsumer resourceChangedConsumer = new ResourceChangedConsumer(resourceChangedService, messageFlags, resourceChangedConfig);

        Message<ResourceChangedData> message = mock(Message.class);
        when(message.getPayload()).thenReturn(new ResourceChangedData());

        resourceChangedConsumer.consume(message);

        verify(resourceChangedService, times(1)).processMessage(any());
    }
    
}
