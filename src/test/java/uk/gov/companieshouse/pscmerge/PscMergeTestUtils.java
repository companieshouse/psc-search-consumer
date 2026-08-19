package uk.gov.companieshouse.pscmerge;

import static uk.gov.companieshouse.common.TestUtils.CONTEXT_ID;
import static uk.gov.companieshouse.common.TestUtils.PSC_ID;

public final class PscMergeTestUtils {

    public static final String PSC_MERGE_TOPIC = "psc-merge";
    public static final String PSC_MERGE_RETRY_TOPIC = "psc-merge-psc-search-consumer-retry";
    public static final String PSC_MERGE_ERROR_TOPIC = "psc-merge-psc-search-consumer-error";
    public static final String PSC_MERGE_INVALID_TOPIC = "psc-merge-psc-search-consumer-invalid";
    public static final String PREVIOUS_PSC_ID = "previous_psc_id";
    public static final String PSC_NOTIFICATIONS_LINK_MERGE = "/psc/previous_psc_id/notifications";

    private PscMergeTestUtils() {}

    public static final PscMerge PSC_MERGE_MESSAGE_PAYLOAD = PscMerge.newBuilder()
            .setPscId(PSC_ID)
            .setContextId(CONTEXT_ID)
            .setPreviousPscId(PREVIOUS_PSC_ID)
            .build();
}
