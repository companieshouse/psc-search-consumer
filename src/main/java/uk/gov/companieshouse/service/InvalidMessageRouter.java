package uk.gov.companieshouse.service;

import java.util.Map;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.util.MessageFlags;
import uk.gov.companieshouse.stream.ResourceChangedData;

import static uk.gov.companieshouse.Application.NAMESPACE;

/**
 * Routes a message to the invalid letter topic if a non-retryable error has been thrown during message processing.
 */
public class InvalidMessageRouter implements ProducerInterceptor<String, ResourceChangedData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private MessageFlags messageFlags;
    private String invalidMessageTopic;

    @Override
    public ProducerRecord<String, ResourceChangedData> onSend(
            ProducerRecord<String, ResourceChangedData> producerRecord) {
        if (messageFlags.isRetryable()) {
            messageFlags.destroy();
            return producerRecord;
        } else {
            final var message = producerRecord.value();
            final var resourceId = message.getResourceId();
            final var resourceKind = message.getResourceKind();
            final var resourceUri = message.getResourceUri();
            LOGGER.error("Encountered non-retryable exception producing message to topic "
                            + producerRecord.topic() + " for resource ID " + resourceId +
                            ", resource kind " + resourceKind + ", resource URI " + resourceUri
                            + ". Redirecting message to invalid topic " + invalidMessageTopic + ".");
            return new ProducerRecord<>(this.invalidMessageTopic, producerRecord.key(),
                    producerRecord.value());
        }
    }

    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(Map<String, ?> configs) {
        this.messageFlags = (MessageFlags) configs.get("message.flags");
        this.invalidMessageTopic = (String) configs.get("invalid.message.topic");
    }
}
