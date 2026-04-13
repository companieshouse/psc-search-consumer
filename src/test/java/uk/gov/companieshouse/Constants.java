package uk.gov.companieshouse;

import uk.gov.companieshouse.stream.EventRecord;
import uk.gov.companieshouse.stream.ResourceChangedData;

import java.util.Collections;

public class Constants {
    // Extract the 'data' object from the psc JSON string
    public static final String pscData = "{\"address\":{\"address_line_1\":\"Feering Hill\",\"address_line_2\":\"Feering\",\"country\":\"England\",\"locality\":\"Colchester\",\"postal_code\":\"CO5 9PY\",\"premises\":\"122\",\"region\":\"Essex\"},\"country_of_residence\":\"England\",\"date_of_birth\":{\"month\":8,\"year\":1984},\"etag\":\"c55a5d7efa17683f704fa33723c56c3fcf93dc7a\",\"identity_verification_details\":{\"appointment_verification_statement_date\":\"2026-09-11\",\"appointment_verification_statement_due_on\":\"2026-09-25\"},\"kind\":\"individual-person-with-significant-control\",\"links\":{\"self\":\"/company/15130809/persons-with-significant-control/individual/ZJmpdoPuMzX35ogDAr98dHmOdaQ\"},\"name\":\"Mrs Bloodflow Hawes\",\"name_elements\":{\"forename\":\"Bloodflow\",\"surname\":\"Hawes\",\"title\":\"Mrs\"},\"nationality\":\"British\",\"natures_of_control\":[\"ownership-of-shares-25-to-50-percent\"],\"notified_on\":\"2023-09-11\"}";

    public static final ResourceChangedData RESOURCE_CHANGED_DATA =ResourceChangedData.newBuilder()
            .setResourceKind("company-psc-individual")
            .setResourceUri("/company/15130809/persons-with-significant-control/individual/ZJmpdoPuMzX35ogDAr98dHmOdaQ")
            .setResourceId("ZJmpdoPuMzX35ogDAr98dHmOdaQ")
            .setData(pscData)
            .setEvent(new EventRecord("", "changed", Collections.emptyList()))
            .setContextId("test-context-id") // Added to satisfy Avro schema
            .build();
}
