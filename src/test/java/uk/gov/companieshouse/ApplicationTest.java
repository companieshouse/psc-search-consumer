package uk.gov.companieshouse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationTest {

    @Test
    void namespaceConstantIsSet() {
        assertEquals("psc-search-consumer", Application.NAMESPACE);
    }
}


