package uk.gov.companieshouse.common.serdes;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import uk.gov.companieshouse.common.exception.InvalidPayloadException;
import uk.gov.companieshouse.pscmerge.PscMerge;
import uk.gov.companieshouse.stream.EventRecord;
import uk.gov.companieshouse.stream.ResourceChangedData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

class KafkaPayloadDeserialiserTest {

    private static final String PSC_ID = "psc_id";
    private static final String PREVIOUS_PSC_ID = "previous_officer_id";
    private static final String CONTEXT_ID = "context_id";

    @Test
    void testDeserialiseResourceChangedData() {
        try (KafkaPayloadDeserialiser<ResourceChangedData> deserialiser = new KafkaPayloadDeserialiser<>(
                ResourceChangedData.class)) {

            //given
            ResourceChangedData changeData = new ResourceChangedData("resource_kind",
                    "resource_uri", "context_id", "resource_id", "data",
                    new EventRecord("published_at", "event_type", Collections.emptyList()));

            //when
            ResourceChangedData actual = deserialiser.deserialize("topic",
                    writePayloadToBytes(changeData, ResourceChangedData.class));

            //then
            assertThat(actual, is(equalTo(changeData)));
        }
    }

    @Test
    void testDeserialisePscMerge() {
        try (KafkaPayloadDeserialiser<PscMerge> deserialiser = new KafkaPayloadDeserialiser<>(PscMerge.class)) {

            //given
            PscMerge pscMerge = new PscMerge(PSC_ID, PREVIOUS_PSC_ID, CONTEXT_ID);

            //when
            PscMerge actual = deserialiser.deserialize("topic", writePayloadToBytes(pscMerge, PscMerge.class));

            //then
            assertThat(actual, is(equalTo(pscMerge)));
        }
    }

    @Test
    void testDeserialiseDataThrowsInvalidPayloadExceptionIfIOExceptionEncountered() {
        //given
        try (KafkaPayloadDeserialiser<ResourceChangedData> deserialiser = new KafkaPayloadDeserialiser<>(
                ResourceChangedData.class)) {

            //when
            Executable actual = () -> deserialiser.deserialize("topic", writePayloadToBytes(
                    "hello", String.class));

            //then
            InvalidPayloadException exception = assertThrows(InvalidPayloadException.class, actual);
            // Note the '\n' is the length prefix of the invalid data sent to the deserialiser
            assertThat(exception.getMessage(), is(equalTo("Invalid payload: [\nhello] was provided.")));
            assertThat(exception.getCause(), is(CoreMatchers.instanceOf(IOException.class)));
        }
    }

    @Test
    void testDeserialiseDataThrowsInvalidPayloadExceptionIfAvroRuntimeExceptionEncountered() {
        //given
        try (KafkaPayloadDeserialiser<ResourceChangedData> deserialiser = new KafkaPayloadDeserialiser<>(
                ResourceChangedData.class)) {

            //when
            Executable actual = () -> deserialiser.deserialize("topic", "invalid".getBytes(StandardCharsets.UTF_8));

            //then
            InvalidPayloadException exception = assertThrows(InvalidPayloadException.class, actual);
            assertThat(exception.getMessage(), is(equalTo("Invalid payload: [invalid] was provided.")));
            assertThat(exception.getCause(), is(CoreMatchers.instanceOf(AvroRuntimeException.class)));
        }
    }

    private static <T> byte[] writePayloadToBytes(T data, Class<T> type) {
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
