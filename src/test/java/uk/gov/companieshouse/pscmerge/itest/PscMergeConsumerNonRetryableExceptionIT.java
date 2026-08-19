package uk.gov.companieshouse.pscmerge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.common.TestUtils.writePayloadToBytes;
import static uk.gov.companieshouse.pscmerge.PscMergeTestUtils.PSC_MERGE_ERROR_TOPIC;
import static uk.gov.companieshouse.pscmerge.PscMergeTestUtils.PSC_MERGE_INVALID_TOPIC;
import static uk.gov.companieshouse.pscmerge.PscMergeTestUtils.PSC_MERGE_MESSAGE_PAYLOAD;
import static uk.gov.companieshouse.pscmerge.PscMergeTestUtils.PSC_MERGE_RETRY_TOPIC;
import static uk.gov.companieshouse.pscmerge.PscMergeTestUtils.PSC_MERGE_TOPIC;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.common.exception.NonRetryableException;
import uk.gov.companieshouse.common.itest.AbstractKafkaTest;
import uk.gov.companieshouse.pscmerge.PscMerge;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test_main_nonretryable")
public class PscMergeConsumerNonRetryableExceptionIT extends AbstractKafkaTest {

    @MockitoBean
    private PscMergeService service;

    @DynamicPropertySource
    public static void props(DynamicPropertyRegistry registry) {
        registry.add("steps", () -> 1);
        registry.add("psc-merge.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Override
    public List<String> getSubscribedTopics() {
        return List.of(PSC_MERGE_TOPIC, PSC_MERGE_RETRY_TOPIC, PSC_MERGE_ERROR_TOPIC, PSC_MERGE_INVALID_TOPIC);
    }

    @Test
    void testRepublishToInvalidMessageTopicIfNonRetryableExceptionThrown() throws Exception {
        doThrow(NonRetryableException.class).when(service).processMessage(any());

        //when
        testProducer.send(new ProducerRecord<>(PSC_MERGE_TOPIC, 0, System.currentTimeMillis(), "key",
                writePayloadToBytes(PSC_MERGE_MESSAGE_PAYLOAD, PscMerge.class)));

        if (!consumerAspect.getLatch().await(5L, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }

        //then
        ConsumerRecords<?, ?> consumerRecords = KafkaTestUtils.getRecords(testConsumer, Duration.ofMillis(10000L), 2);
        assertThat(recordsPerTopic(consumerRecords, PSC_MERGE_TOPIC)).isOne();
        assertThat(recordsPerTopic(consumerRecords, PSC_MERGE_RETRY_TOPIC)).isZero();
        assertThat(recordsPerTopic(consumerRecords, PSC_MERGE_ERROR_TOPIC)).isZero();
        assertThat(recordsPerTopic(consumerRecords, PSC_MERGE_INVALID_TOPIC)).isOne();
        verify(service).processMessage(any());
    }
}
