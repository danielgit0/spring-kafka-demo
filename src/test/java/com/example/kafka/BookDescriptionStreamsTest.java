package com.example.kafka;

import static com.example.kafka.common.topics.BookDescriptionTopic.BOOK_DESCRIPTION_V1;
import static com.example.kafka.common.topics.BookDescriptionTopic.BOOK_DESCRIPTION_V1_STORE;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.generated.kafka.BookDescription;
import java.time.Duration;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(
    properties = {
      "spring.kafka.properties.schema.registry.url=mock://test-registry",
      "spring.kafka.streams.properties.schema.registry.url=mock://test-registry"
    })
public class BookDescriptionStreamsTest {

  @Autowired private KafkaTemplate<String, BookDescription> kafkaTemplate;

  @Autowired private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

  private ReadOnlyKeyValueStore<String, BookDescription> stateStore;

  @BeforeEach
  void setUp() {
    KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
    assertNotNull(kafkaStreams, "Kafka Streams instance should be initialized");

    stateStore =
        kafkaStreams.store(
            StoreQueryParameters.fromNameAndType(
                BOOK_DESCRIPTION_V1_STORE, QueryableStoreTypes.keyValueStore()));
  }

  @Test
  void testConsumeUpdateAndTombstoneLifecycle() {
    String key = "978-3-16-148410-0";

    // --- 1. CONSUME (Initial Create Event) ---
    BookDescription createPayload =
        BookDescription.newBuilder().setDescription("Initial Draft Version").build();

    kafkaTemplate.send(BOOK_DESCRIPTION_V1, key, createPayload);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              BookDescription record = stateStore.get(key);
              assertNotNull(record);
              assertEquals("Initial Draft Version", record.getDescription());
            });

    // --- 2. UPDATE (Upsert Event) ---
    BookDescription updatePayload =
        BookDescription.newBuilder().setDescription("Updated Production Version").build();

    kafkaTemplate.send(BOOK_DESCRIPTION_V1, key, updatePayload);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              BookDescription record = stateStore.get(key);
              assertNotNull(record);
              assertEquals("Updated Production Version", record.getDescription());
            });

    // --- 3. TOMBSTONE (Delete Event) ---
    // Producing a null value to the key signals deletion to the KTable
    kafkaTemplate.send(BOOK_DESCRIPTION_V1, key, null);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () -> {
              BookDescription record = stateStore.get(key);
              assertNull(record, "The record should be completely removed from the state store");
            });
  }
}
