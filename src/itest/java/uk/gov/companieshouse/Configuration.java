package uk.gov.companieshouse;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@CucumberContextConfiguration
@AutoConfigureMockMvc
@ActiveProfiles("test_main_positive")
@Import({TestKafkaConfig.class, KafkaContainerConfig.class})
public class Configuration {
}
