package uk.gov.companieshouse.pscmerge.service;

import org.springframework.messaging.Message;
import uk.gov.companieshouse.pscmerge.PscMerge;

public interface MergeService {

    void processMessage(Message<PscMerge> message);
}
