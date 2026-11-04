package uk.gov.companieshouse.resourcechanged.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ResourceChangedConfigTest {

    @Test
    void configureAvroClassSecuritySetsAllowAllValidator() {
        assertDoesNotThrow(ResourceChangedConfig::configureAvroClassSecurity);
    }
}

