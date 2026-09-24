package uk.gov.companieshouse.pscmerge.itest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.common.TestUtils.writePayloadToBytes;
import static uk.gov.companieshouse.pscmerge.PscMergeTestUtils.PREVIOUS_PSC_ID;
import static uk.gov.companieshouse.pscmerge.PscMergeTestUtils.PSC_MERGE_ERROR_TOPIC;
import static uk.gov.companieshouse.pscmerge.PscMergeTestUtils.PSC_MERGE_INVALID_TOPIC;
import static uk.gov.companieshouse.pscmerge.PscMergeTestUtils.PSC_MERGE_MESSAGE_PAYLOAD;
import static uk.gov.companieshouse.pscmerge.PscMergeTestUtils.PSC_MERGE_RETRY_TOPIC;
import static uk.gov.companieshouse.pscmerge.PscMergeTestUtils.PSC_MERGE_TOPIC;
import static uk.gov.companieshouse.pscmerge.PscMergeTestUtils.PSC_NOTIFICATIONS_LINK_MERGE;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.companieshouse.api.psc_notifications.NotificationList;
import uk.gov.companieshouse.common.client.NotificationsApiClient;
import uk.gov.companieshouse.common.client.PrimarySearchApiClient;
import uk.gov.companieshouse.common.itest.AbstractKafkaTest;
import uk.gov.companieshouse.pscmerge.PscMerge;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test_main_positive")
class PscMergeDeleteOutcomeIT extends AbstractKafkaTest {

    @MockitoBean
    private NotificationsApiClient notificationsApiClient;

    @MockitoBean
    private PrimarySearchApiClient primarySearchApiClient;

    private final ConcurrentHashMap<String, String> simulatedPrimarySearchIndex = new ConcurrentHashMap<>();

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("steps", () -> 1);
        registry.add("KAFKA3_BROKER_ADDR", kafka::getBootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Override
    public List<String> getSubscribedTopics() {
        return List.of(PSC_MERGE_TOPIC, PSC_MERGE_RETRY_TOPIC, PSC_MERGE_ERROR_TOPIC, PSC_MERGE_INVALID_TOPIC);
    }

    @BeforeEach
    void setUpScenario() {
        simulatedPrimarySearchIndex.clear();
        simulatedPrimarySearchIndex.put(PREVIOUS_PSC_ID, "present-before-merge");

        when(notificationsApiClient.getPscNotificationListForDelete(anyString())).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            simulatedPrimarySearchIndex.remove(invocation.getArgument(0, String.class));
            return null;
        }).when(primarySearchApiClient).deletePsc(anyString());
        doAnswer(invocation -> {
            NotificationList notificationList = invocation.getArgument(1, NotificationList.class);
            simulatedPrimarySearchIndex.put(invocation.getArgument(0, String.class), notificationList.toString());
            return null;
        }).when(primarySearchApiClient).upsertPsc(anyString(), org.mockito.ArgumentMatchers.any(NotificationList.class));
    }

    @Test
    void shouldDeletePreviousPscAndMakeItUnsearchableWhenDeleteNotificationsLookupIsEmpty() throws Exception {
        testProducer.send(new ProducerRecord<>(PSC_MERGE_TOPIC, 0, System.currentTimeMillis(), "key",
                writePayloadToBytes(PSC_MERGE_MESSAGE_PAYLOAD, PscMerge.class)));
        if (!consumerAspect.getLatch().await(5L, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }

        ConsumerRecords<?, ?> records = KafkaTestUtils.getRecords(testConsumer, Duration.ofMillis(10000L), 1);
        assertThat(recordsPerTopic(records, PSC_MERGE_TOPIC)).isOne();
        assertThat(recordsPerTopic(records, PSC_MERGE_RETRY_TOPIC)).isZero();
        assertThat(recordsPerTopic(records, PSC_MERGE_ERROR_TOPIC)).isZero();
        assertThat(recordsPerTopic(records, PSC_MERGE_INVALID_TOPIC)).isZero();

        verify(notificationsApiClient).getPscNotificationListForDelete(PSC_NOTIFICATIONS_LINK_MERGE);
        verify(primarySearchApiClient).deletePsc(PREVIOUS_PSC_ID);
        verify(primarySearchApiClient, never()).upsertPsc(anyString(), org.mockito.ArgumentMatchers.any(NotificationList.class));

        // Simulated index lookup proves the post-delete outcome expected by the AC.
        assertThat(simulatedPrimarySearchIndex).doesNotContainKey(PREVIOUS_PSC_ID);
    }
}


