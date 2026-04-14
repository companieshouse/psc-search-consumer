package uk.gov.companieshouse.logging;

import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.kafka.retrytopic.RetryTopicHeaders;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import uk.gov.companieshouse.exception.RetryableException;
import uk.gov.companieshouse.stream.ResourceChangedData;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageLoggingAspectTest {

    private MessageLoggingAspect aspect;

    @Mock
    private MessageHeaders headers;

    @Mock
    private JoinPoint joinPoint;

    @BeforeEach
    void setUp() {
        aspect = new MessageLoggingAspect(5);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void testLogBeforeMainConsumer_logsExpectedMessage(CapturedOutput capture) {
        ResourceChangedData data = mock(ResourceChangedData.class);
        when(data.getResourceId()).thenReturn("resource123");
        when(data.getContextId()).thenReturn("context456");

        Map<String, Object> headersMap = new HashMap<>();
        headersMap.put(KafkaHeaders.RECEIVED_TOPIC, "test-topic");
        headersMap.put(KafkaHeaders.RECEIVED_PARTITION, 1);
        headersMap.put(KafkaHeaders.OFFSET, 42L);
        headersMap.put("spring_retry_attempts", ByteBuffer.allocate(4).putInt(2).array());

        MessageHeaders testHeaders = new MessageHeaders(headersMap);
        Message<ResourceChangedData> message = mock(Message.class);
        when(message.getHeaders()).thenReturn(testHeaders);
        when(message.getPayload()).thenReturn(data);

        JoinPoint jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[] { message });

        // when
        aspect.logBeforeMainConsumer(jp);

        //then
        assertTrue(capture.getOut().contains("Processing delta"));
        assertTrue(capture.getOut().contains("resource123"));
        assertTrue(capture.getOut().contains("context456"));
        assertTrue(capture.getOut().contains("test-topic"));
    }

    @Test
    void testLogMesssage_catchesRuntimeException(){
        Message<?> message = mock(Message.class);
        MessageHeaders headers = mock(MessageHeaders.class);

        when(message.getHeaders()).thenReturn(headers);
        when(headers.get(anyString())).thenThrow(new RuntimeException("Error"));

        JoinPoint jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[] {message});

        MessageLoggingAspect aspect = new MessageLoggingAspect(3);

        assertThrows(RuntimeException.class, () -> aspect.logBeforeMainConsumer(jp));
    }

    @Test
    void testLogBeforeMainConsumer_retryableException_maxAttemptsReached() {
        Message<?> message = mock(Message.class);
        MessageHeaders headers = mock(MessageHeaders.class);

        when(message.getHeaders()).thenReturn(headers);
        when(headers.get(RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS))
                .thenThrow(new RetryableException("Retryable error"));

        JoinPoint jp = mock(JoinPoint.class);
        when(jp.getArgs()).thenReturn(new Object[] {message});
        MessageLoggingAspect aspect = new MessageLoggingAspect(3);

        assertThrows(RetryableException.class, () -> aspect.logBeforeMainConsumer(jp));
    }

}
