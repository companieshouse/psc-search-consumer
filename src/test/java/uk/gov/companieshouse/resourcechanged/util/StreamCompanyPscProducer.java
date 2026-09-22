package uk.gov.companieshouse.resourcechanged.util;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.common.itest.TestKafkaConfig;
import uk.gov.companieshouse.stream.ResourceChangedData;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static uk.gov.companieshouse.common.TestUtils.RESOURCE_CHANGED_DATA;


/**
 * "Test" class re-purposed to produce {@link ResourceChangedData} messages to the
 * <code>stream-company-psc</code> topic in Docker. This is NOT to be run as part
 * of an automated test suite. It is for manual testing only.
 */
@SpringBootTest
@Import(TestKafkaConfig.class)
@TestPropertySource(locations="classpath:application-psc-search-consumer-in-docker.properties")
@Tag("manual")
@SuppressWarnings("squid:S3577") // This is NOT to be run as part of an automated test suite.
class StreamCompanyPscProducer {


    private static final Logger LOGGER = LoggerFactory.getLogger("StreamCompanyPscProducer");

    private static final int MESSAGE_WAIT_TIMEOUT_SECONDS = 10;

    private static final String SAME_PARTITION_KEY = "key";

    private String streamCompanyPscTopic = "stream-company-psc";

    @Autowired
    private KafkaProducer<String, ResourceChangedData> testProducer;

    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    void produceMessageToDocker() throws InterruptedException, ExecutionException, TimeoutException {
        final var future = testProducer.send(new ProducerRecord<>(
                streamCompanyPscTopic, 0, System.currentTimeMillis(), SAME_PARTITION_KEY, RESOURCE_CHANGED_DATA));
        future.get(MESSAGE_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        LOGGER.info("Message " + RESOURCE_CHANGED_DATA + " delivered to topic " + streamCompanyPscTopic);
    }
}
