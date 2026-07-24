package uk.gov.companieshouse.pscmerge;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.common.exception.MessageFlags;
import uk.gov.companieshouse.common.exception.RetryableException;
import uk.gov.companieshouse.pscmerge.service.MergeService;
import uk.gov.companieshouse.pscmerge.service.PscMergeService;

@Component
public class PscMergeConsumer {

    private final MergeService router;
    private final MessageFlags messageFlags;

    public PscMergeConsumer(PscMergeService router, MessageFlags messageFlags) {
        this.router = router;
        this.messageFlags = messageFlags;
    }

    @KafkaListener(
            id = "${PSC_MERGE_TOPIC:psc-merge}-consumer",
            containerFactory = "pscMergeKafkaListenerContainerFactory",
            topics = "${PSC_MERGE_TOPIC:psc-merge}",
            groupId = "${GROUP_ID:psc-merge-consumer}"
    )
    public void consume(Message<PscMerge> message) {
        try {
            router.processMessage(message);
        } catch (RetryableException e) {
            messageFlags.setRetryable(true);
            throw e;
        }

    }
}
