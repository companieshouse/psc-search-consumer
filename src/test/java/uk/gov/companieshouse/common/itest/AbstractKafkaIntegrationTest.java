package uk.gov.companieshouse.common.itest;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@Import(TestKafkaConfig.class)
public abstract class AbstractKafkaIntegrationTest {

    @Container
    protected static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse(
            "confluentinc/cp-kafka:latest")).withKraft();

    @Autowired
    protected ConsumerAspect consumerAspect;

    @BeforeEach
    void resetConsumerLatch() {
        consumerAspect.resetLatch();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
}