package uk.gov.companieshouse.resourcechanged.itest;

import org.apache.avro.util.ClassSecurityValidator;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.common.TestUtils;
import uk.gov.companieshouse.common.exception.NonRetryableException;
import uk.gov.companieshouse.common.itest.AbstractKafkaIntegrationTest;
import uk.gov.companieshouse.resourcechanged.service.PscSearchUpdaterServiceRouter;
import uk.gov.companieshouse.resourcechanged.service.ResourceChangedServiceParameters;
import uk.gov.companieshouse.stream.ResourceChangedData;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.common.TestUtils.ERROR_TOPIC;
import static uk.gov.companieshouse.common.TestUtils.INVALID_TOPIC;
import static uk.gov.companieshouse.common.TestUtils.MAIN_TOPIC;
import static uk.gov.companieshouse.common.TestUtils.RESOURCE_CHANGED_DATA;
import static uk.gov.companieshouse.common.TestUtils.RETRY_TOPIC;

@SpringBootTest
@ActiveProfiles("test_main_nonretryable")
class ResourceChangedConsumerNonRetryableExceptionTest extends AbstractKafkaIntegrationTest {

    @Autowired
    private KafkaProducer<String, ResourceChangedData> testProducer;
    @Autowired
    private KafkaConsumer<String, ResourceChangedData> testConsumer;

    @MockitoBean
    private PscSearchUpdaterServiceRouter router;

    @BeforeAll
    static void setupAvroSecurity() {
        // Avro 1.12+ requires explicit class trust for serialization.
        // This accepts all classes. Long term, we could consider using the schema registry to avoid
        // runtime class validation entirely. However, that would make integration testing more complex.
        ClassSecurityValidator.setGlobal((clazz -> true));
    }

    @BeforeEach
    public void drainKafkaTopics() {
        testConsumer.poll(Duration.ofSeconds(1));
    }

    @Test
    void testRepublishToInvalidMessageTopicIfNonRetryableExceptionThrown() throws InterruptedException {
        //given
        doThrow(NonRetryableException.class).when(router).route(any());

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
        verify(router).route(new ResourceChangedServiceParameters(RESOURCE_CHANGED_DATA));
    }
}
