package uk.gov.companieshouse.resourcechanged;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.messaging.Message;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.companieshouse.common.exception.RetryableException;
import uk.gov.companieshouse.resourcechanged.service.ResourceChangedService;
import uk.gov.companieshouse.resourcechanged.service.ResourceChangedServiceParameters;
import uk.gov.companieshouse.stream.ResourceChangedData;
import uk.gov.companieshouse.common.exception.MessageFlags;
import uk.gov.companieshouse.resourcechanged.config.ResourceChangedConfig;

/**
 * Consumes messages from the configured main Kafka topic.
 */
@Component
public class ResourceChangedConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceChangedConsumer.class);
    private final ResourceChangedService resourceChangedService;
    private final MessageFlags messageFlags;
    private final ResourceChangedConfig resourceChangedConfig;

    public ResourceChangedConsumer(ResourceChangedService resourceChangedService, MessageFlags messageFlags, ResourceChangedConfig resourceChangedConfig) {
        this.resourceChangedService = resourceChangedService;
        this.messageFlags = messageFlags;
        this.resourceChangedConfig = resourceChangedConfig;
    }

    /**
     * Consume a message from the main Kafka topic.
     *
     * @param message A message containing a payload.
     */
    @KafkaListener(
            id = "${consumer.group_id}",
            containerFactory = "kafkaListenerContainerFactory",
            topics = "${consumer.topic}",
            groupId = "${consumer.group_id}"
    )
    @RetryableTopic(
            attempts = "${consumer.max_attempts}",
            autoCreateTopics = "false",
            backoff = @Backoff(delayExpression = "${consumer.backoff_delay}"),
            retryTopicSuffix = "-${consumer.group_id}-retry",
            dltTopicSuffix = "-${consumer.group_id}-error",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.SINGLE_TOPIC,
            include = RetryableException.class
    )
    public void consume(Message<ResourceChangedData> message) {
        if (!resourceChangedConfig.isPscConsumerEnabled()) {
            LOGGER.info("PSC consumer is disabled by feature flag. Message will not be processed.");
            return;
        }
        try {
            resourceChangedService.processMessage(new ResourceChangedServiceParameters(message.getPayload()));
        } catch (RetryableException e) {
            messageFlags.setRetryable(true);
            throw e;
        }
    }
}
 