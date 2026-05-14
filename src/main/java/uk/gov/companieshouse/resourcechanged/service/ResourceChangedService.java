package uk.gov.companieshouse.resourcechanged.service;

/**
 * Processes an incoming message.
 */
public interface ResourceChangedService {

    /**
     * Processes an incoming message.
     *
     * @param parameters Any parameters required when processing the message.
     */
    void processMessage(ResourceChangedServiceParameters parameters);
}
