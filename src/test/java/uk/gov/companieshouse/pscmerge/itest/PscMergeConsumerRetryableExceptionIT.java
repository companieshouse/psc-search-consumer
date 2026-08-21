package uk.gov.companieshouse.pscmerge.itest;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.common.exception.RetryableException;
import uk.gov.companieshouse.common.itest.AbstractKafkaTest;
import uk.gov.companieshouse.pscmerge.PscMerge;
import uk.gov.companieshouse.pscmerge.service.PscMergeService;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.common.TestUtils.writePayloadToBytes;
import static uk.gov.companieshouse.pscmerge.PscMergeTestUtils.PSC_MERGE_ERROR_TOPIC;
import static uk.gov.companieshouse.pscmerge.PscMergeTestUtils.PSC_MERGE_INVALID_TOPIC;
import static uk.gov.companieshouse.pscmerge.PscMergeTestUtils.PSC_MERGE_MESSAGE_PAYLOAD;
import static uk.gov.companieshouse.pscmerge.PscMergeTestUtils.PSC_MERGE_RETRY_TOPIC;
import static uk.gov.companieshouse.pscmerge.PscMergeTestUtils.PSC_MERGE_TOPIC;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test_main_retryable")
public class PscMergeConsumerRetryableExceptionIT extends AbstractKafkaTest {

    @MockitoBean
    private PscMergeService service;

    @Captor
    private ArgumentCaptor<Message<PscMerge>> messageArgumentCaptor;

    @DynamicPropertySource
    public static void props(DynamicPropertyRegistry registry) {
        registry.add("steps", () -> 1);
        registry.add("KAFKA3_BROKER_ADDR", kafka::getBootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Override
    public List<String> getSubscribedTopics() {
        return List.of(PSC_MERGE_TOPIC, PSC_MERGE_RETRY_TOPIC, PSC_MERGE_ERROR_TOPIC, PSC_MERGE_INVALID_TOPIC);
    }

    @Test
    void testRepublishToErrorTopicThroughRetryTopics() throws Exception {
        //given
        doThrow(RetryableException.class).when(service).processMessage(any());

        //when
        testProducer.send(
                new ProducerRecord<>(PSC_MERGE_TOPIC, 0, System.currentTimeMillis(), "key",
                        writePayloadToBytes(PSC_MERGE_MESSAGE_PAYLOAD, PscMerge.class)));
        if (!consumerAspect.getLatch().await(30L, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }

        //then
        ConsumerRecords<?, ?> records = KafkaTestUtils.getRecords(testConsumer, Duration.ofMillis(10000L), 6);
        assertThat(recordsPerTopic(records, PSC_MERGE_TOPIC)).isOne();
        assertThat(recordsPerTopic(records, PSC_MERGE_RETRY_TOPIC)).isEqualTo(4);
        assertThat(recordsPerTopic(records, PSC_MERGE_ERROR_TOPIC)).isOne();
        assertThat(recordsPerTopic(records, PSC_MERGE_INVALID_TOPIC)).isZero();
        verify(service, times(5)).processMessage(messageArgumentCaptor.capture());
        assertThat(messageArgumentCaptor.getValue().getPayload()).isEqualTo(PSC_MERGE_MESSAGE_PAYLOAD);
    }
}
