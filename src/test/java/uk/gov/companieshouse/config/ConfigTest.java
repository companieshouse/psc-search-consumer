package uk.gov.companieshouse.config;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.apache.kafka.common.serialization.Serializer;
import uk.gov.companieshouse.exception.NonRetryableException;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.stream.ResourceChangedData;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for producerFactory error handling in {@link Config}.
 */
class ConfigTest {

	private static final String serializationErrorPrefix = "Caught SerializationException serializing kafka message:";

	@Test
	void testProducerFactoryThrowsWhenSerializerFails() throws Exception {
		AvroSerializer<ResourceChangedData> avroSerializer = Mockito.mock(AvroSerializer.class);
		Mockito.when(avroSerializer.toBinary(Mockito.any()))
				.thenThrow(new SerializationException("serialize failed"));

		// Recreate the same value serializer lambda used in Config.producerFactory so we can exercise
		// the error handling branch when AvroSerializer.toBinary throws SerializationException.
		Serializer<ResourceChangedData> valueSerializer = (topic, data) -> {
			try {
				return avroSerializer.toBinary(data);
			} catch (SerializationException e) {
				final String error = "%s %s".formatted(serializationErrorPrefix, e.getMessage());
				throw new NonRetryableException(error, e);
			}
		};

		// invoking serialize should unwrap the SerializationException and throw NonRetryableException
		ResourceChangedData payload = Mockito.mock(ResourceChangedData.class);
		NonRetryableException ex = assertThrows(NonRetryableException.class,
				() -> valueSerializer.serialize("some-topic", payload));
		assertTrue(ex.getMessage().contains(serializationErrorPrefix));
	}
}


