package uk.gov.companieshouse.resourcechanged.service;

import uk.gov.companieshouse.stream.ResourceChangedData;

import java.util.Objects;

/**
 * Contains all parameters required by {@link ResourceChangedService service implementations}.
 */
public class ResourceChangedServiceParameters {

    private final ResourceChangedData data;

    public ResourceChangedServiceParameters(ResourceChangedData data) {
        this.data = data;
    }

    /**
     * Get data attached to the ServiceParameters object.
     *
     * @return A string representing data that has been attached to the ServiceParameters object.
     */
    public ResourceChangedData getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResourceChangedServiceParameters)) {
            return false;
        }
        ResourceChangedServiceParameters that = (ResourceChangedServiceParameters) o;
        return Objects.equals(getData(), that.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getData());
    }
}
