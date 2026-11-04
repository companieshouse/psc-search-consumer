package uk.gov.companieshouse.pscmerge.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PscMergeKafkaConfigTest {

    @Test
    void configureAvroClassSecuritySetsAllowAllValidator() {
        assertDoesNotThrow(PscMergeKafkaConfig::configureAvroClassSecurity);
    }
}


