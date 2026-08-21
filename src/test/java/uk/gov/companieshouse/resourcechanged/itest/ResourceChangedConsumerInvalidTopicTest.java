package uk.gov.companieshouse.resourcechanged.itest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.companieshouse.common.TestUtils.RESOURCE_CHANGED_DATA;
import static uk.gov.companieshouse.common.TestUtils.ERROR_TOPIC;
import static uk.gov.companieshouse.common.TestUtils.INVALID_TOPIC;
import static uk.gov.companieshouse.common.TestUtils.MAIN_TOPIC;
import static uk.gov.companieshouse.common.TestUtils.RETRY_TOPIC;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.common.TestUtils;
import uk.gov.companieshouse.common.itest.AbstractKafkaIntegrationTest;
import uk.gov.companieshouse.stream.ResourceChangedData;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test_main_nonretryable")
class ResourceChangedConsumerInvalidTopicTest extends AbstractKafkaIntegrationTest {

    @Autowired
    private KafkaProducer<String, ResourceChangedData> testProducer;
    @Autowired
    private KafkaConsumer<String, ResourceChangedData> testConsumer;

    @BeforeEach
    void drainKafkaTopics() {
        testConsumer.poll(Duration.ofSeconds(1));
    }

    @Test
    void testPublishToInvalidMessageTopicIfInvalidDataDeserialised() throws InterruptedException {

        //when
        testProducer.send(new ProducerRecord<>(MAIN_TOPIC, 0, System.currentTimeMillis(), "key",
                RESOURCE_CHANGED_DATA));
        if (!consumerAspect.getLatch().await(5L, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }
        ConsumerRecords<?, ?> consumerRecords = KafkaTestUtils.getRecords(testConsumer, Duration.ofSeconds(10), 2);

        //then
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, MAIN_TOPIC)).isEqualTo(1);
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, RETRY_TOPIC)).isZero();
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, ERROR_TOPIC)).isZero();
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, INVALID_TOPIC)).isEqualTo(1);
    }
}
