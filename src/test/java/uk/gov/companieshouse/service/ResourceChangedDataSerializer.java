package uk.gov.companieshouse.service;

import org.apache.kafka.common.serialization.Serializer;
import uk.gov.companieshouse.stream.ResourceChangedData;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Simple serializer for ResourceChangedData for test purposes.
 * Serializes the object using its toString() method as UTF-8 bytes.
 * Replace with a real serializer if needed.
 */
public class ResourceChangedDataSerializer implements Serializer<ResourceChangedData> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public byte[] serialize(String topic, ResourceChangedData data) {
        return data == null ? null : data.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void close() {}
}

