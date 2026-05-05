package uk.gov.companieshouse.resourcechanged.logging;

import static org.springframework.kafka.retrytopic.RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.common.exception.RetryableException;
import uk.gov.companieshouse.Application;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.resourcechanged.ResourceChangedConsumer;
import uk.gov.companieshouse.stream.ResourceChangedData;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Logs message details before and after it has been processed by
 * the {@link ResourceChangedConsumer main consumer}.<br>
 * <br>
 * Details that will be logged will include:
 * <ul>
 *     <li>The context ID of the message</li>
 *     <li>The topic the message was consumed from</li>
 *     <li>The partition of the topic the message was consumed from</li>
 *     <li>The offset number of the message</li>
 * </ul>
 */
@Component
@Aspect
public class ResourceChangedMessageLoggingAspect {
    private final int maxAttempts;

    public ResourceChangedMessageLoggingAspect(@Value("${consumer.max-attempts}") int maxAttempts) { this.maxAttempts = maxAttempts; }

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.NAMESPACE);

    private static final String LOG_MESSAGE_RECEIVED = "Processing delta";
    private static final String LOG_MESSAGE_PROCESSED = "Processed delta";
    private static final String EXCEPTION_MESSAGE = "%s exception thrown: %s";

    @Before("execution(* uk.gov.companieshouse.resourcechanged.Consumer.consume(..))")
    public void logBeforeMainConsumer(JoinPoint joinPoint) {
        logMessage(LOG_MESSAGE_RECEIVED, (Message<?>)joinPoint.getArgs()[0]);
    }

    @After("execution(* uk.gov.companieshouse.resourcechanged.Consumer.consume(..))")
    void logAfterMainConsumer(JoinPoint joinPoint) {
        logMessage(LOG_MESSAGE_PROCESSED, (Message<?>)joinPoint.getArgs()[0]);
    }

    @AfterThrowing(pointcut = "execution(* uk.gov.companieshouse.resourcechanged.Consumer.consume(..))", throwing = "error")
    public void afterThrowingAdvice(JoinPoint joinPoint, Throwable error) {
        logMessage(String.format(EXCEPTION_MESSAGE, error.getClass().getSimpleName(), error.getMessage()), (Message<?>) joinPoint.getArgs()[0]);
    }

    private void logMessage(String logMessage, Message<?> incomingMessage) {
        int retryCount = 0;
        String requestId = "";
        String resourceId = "";

        try {
            MessageHeaders headers = incomingMessage.getHeaders();
            retryCount = Optional.ofNullable(headers.get(DEFAULT_HEADER_ATTEMPTS))
                    .map(attempts -> ByteBuffer.wrap((byte[]) attempts).getInt())
                    .orElse(1) - 1;

        } catch (RetryableException ex) {
            // maxAttempts includes first attempt which is not a retry
            if (retryCount >= maxAttempts - 1) {
                LOGGER.error("Max retry attempts reached", ex);
            } else {
                LOGGER.info(EXCEPTION_MESSAGE);
            }
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Exception thrown", ex);
            throw ex;
        }

        Object payload = incomingMessage.getPayload();

        if (payload instanceof ResourceChangedData data) {
            resourceId = data.getResourceId();
            requestId = data.getContextId();
        }

        String topic = Optional.ofNullable((String) incomingMessage.getHeaders().get(KafkaHeaders.RECEIVED_TOPIC))
                .orElse("no topic");
        Integer partition = Optional.ofNullable((Integer) incomingMessage.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION))
                .orElse(0);
        Long offset = Optional.ofNullable((Long) incomingMessage.getHeaders().get(KafkaHeaders.OFFSET))
                .orElse(0L);
        LOGGER.debug(logMessage, new HashMap<>(Map.of(
                "topic", topic,
                "partition", partition,
                "offset", offset,
                "retryCount", retryCount,
                "notification_id", resourceId,
                "request_id", requestId)));
    }
}
