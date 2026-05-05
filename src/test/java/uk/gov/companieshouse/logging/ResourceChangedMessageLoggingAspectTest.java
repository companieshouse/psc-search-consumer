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
import uk.gov.companieshouse.common.exception.RetryableException;
import uk.gov.companieshouse.resourcechanged.logging.ResourceChangedMessageLoggingAspect;
import uk.gov.companieshouse.stream.ResourceChangedData;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceChangedMessageLoggingAspectTest {

    @Mock
    ResourceChangedMessageLoggingAspect aspect = new ResourceChangedMessageLoggingAspect(5);

    @Mock
    JoinPoint jp = mock(JoinPoint.class);

    @Mock
    Message<ResourceChangedData> messageChanged = mock(Message.class);

    @Mock
    ResourceChangedData data = mock(ResourceChangedData.class);

    @Mock
    MessageHeaders headers = mock(MessageHeaders.class);

    @BeforeEach
    void setUp() {
        aspect = new ResourceChangedMessageLoggingAspect(5);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void testLogBeforeMainConsumer_logsExpectedMessage(CapturedOutput capture) {
        when(data.getResourceId()).thenReturn("resource123");
        when(data.getContextId()).thenReturn("context456");

        Map<String, Object> headersMap = new HashMap<>();
        headersMap.put(KafkaHeaders.RECEIVED_TOPIC, "test-topic");
        headersMap.put(KafkaHeaders.RECEIVED_PARTITION, 1);
        headersMap.put(KafkaHeaders.OFFSET, 42L);
        headersMap.put("spring_retry_attempts", ByteBuffer.allocate(4).putInt(2).array());

        MessageHeaders testHeaders = new MessageHeaders(headersMap);
        when(messageChanged.getHeaders()).thenReturn(testHeaders);
        when(messageChanged.getPayload()).thenReturn(data);

        when(jp.getArgs()).thenReturn(new Object[] { messageChanged });

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
        when(messageChanged.getHeaders()).thenReturn(headers);
        when(headers.get(anyString())).thenThrow(new RuntimeException("Error"));

        when(jp.getArgs()).thenReturn(new Object[] { messageChanged });

        assertThrows(RuntimeException.class, () -> aspect.logBeforeMainConsumer(jp));
    }

    @Test
    void testLogBeforeMainConsumer_retryableException_maxAttemptsReached() {
        when(messageChanged.getHeaders()).thenReturn(headers);
        when(headers.get(RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS))
                .thenThrow(new RetryableException("Retryable error"));

        when(jp.getArgs()).thenReturn(new Object[] { messageChanged });

        assertThrows(RetryableException.class, () -> aspect.logBeforeMainConsumer(jp));
    }

}
