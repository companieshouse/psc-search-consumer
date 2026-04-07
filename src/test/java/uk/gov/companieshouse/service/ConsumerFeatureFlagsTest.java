package uk.gov.companieshouse.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import uk.gov.companieshouse.config.Config;
import uk.gov.companieshouse.util.MessageFlags;

public class ConsumerFeatureFlagsTest {

    @Test
    void doesNotProcessMessagesWhenFlagDisabled() {
        Service service = mock(Service.class);
        MessageFlags messageFlags = mock(MessageFlags.class);
        Config config = mock(Config.class);
        when(config.isPscConsumerEnabled()).thenReturn(false);

        Consumer consumer = new Consumer(service, messageFlags, config);

        Message<String> message = mock(Message.class);
        when(message.getPayload()).thenReturn("test");

        consumer.consume(message);

        verify(service, never()).processMessage(any());
    }

    @Test
    void processesMessagesWhenFlagEnabled() {
        Service service = mock(Service.class);
        MessageFlags messageFlags = mock(MessageFlags.class);
        Config config = mock(Config.class);
        when(config.isPscConsumerEnabled()).thenReturn(true);

        Consumer consumer = new Consumer(service, messageFlags, config);

        Message<String> message = mock(Message.class);
        when(message.getPayload()).thenReturn("test");

        consumer.consume(message);

        verify(service, times(1)).processMessage(any());
    }
    
}
