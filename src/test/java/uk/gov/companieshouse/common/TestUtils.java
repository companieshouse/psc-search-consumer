package uk.gov.companieshouse.common;

import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;
import uk.gov.companieshouse.stream.EventRecord;
import uk.gov.companieshouse.stream.ResourceChangedData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.util.Collections.emptyList;

public final class TestUtils {

    public static final String MAIN_TOPIC = "echo";
    public static final String RETRY_TOPIC = "echo-echo-consumer-retry";
    public static final String ERROR_TOPIC = "echo-echo-consumer-error";
    public static final String INVALID_TOPIC = "echo-echo-consumer-invalid";

    public static final String DELETE_PSC_API_CALL = "PSC Search API DELETE";
    public static final String PSC_SEARCH_LINK = "/persons-with-significant-control-search/persons-with-significant-control/123";
    public static final ResourceChangedData RESOURCE_CHANGED_DATA;

    public static final String CONTEXT_ID = "context_id";
    public static final String PSC_ID = "psc_id";

    static {
        try {
            RESOURCE_CHANGED_DATA = ResourceChangedData.newBuilder()
                    .setResourceKind("company-psc-individual")
                    .setResourceUri("/company/15130809/persons-with-significant-control/individual/ZJmpdoPuMzX35ogDAr98dHmOdaQ")
                    .setResourceId("ZJmpdoPuMzX35ogDAr98dHmOdaQ")
                    .setData(IOUtils.resourceToString("/json/resource-changed-data.json",
                            StandardCharsets.UTF_8))
                    .setEvent(getEvent("deleted"))
                    .setContextId("22-usZuMZEnZY6W_Kip1539964678")
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static EventRecord getEvent(String type) {
        return EventRecord.newBuilder()
                .setPublishedAt("1453896193333")
                .setType(type)
                .setFieldsChanged(emptyList())
                .build();
    }

    private TestUtils(){
    }

    public static int noOfRecordsForTopic(ConsumerRecords<?, ?> records, String topic) {
        int count = 0;
        for (ConsumerRecord<?, ?> ignored : records.records(topic)) {
            count++;
        }
        return count;
    }

    public static <T> byte[] writePayloadToBytes(T data, Class<T> type) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Encoder encoder = EncoderFactory.get().directBinaryEncoder(outputStream, null);
            DatumWriter<T> writer = new ReflectDatumWriter<>(type);
            writer.write(data, encoder);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
