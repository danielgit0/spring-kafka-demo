package com.example.kafka.common.topics;

import com.example.generated.kafka.BookDescription;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableKafkaStreams
public class BookDescriptionTopic {

  public static final String BOOK_DESCRIPTION_V1 =
      "book-description.local.kafka_demo.book-description.v1";
  public static final String BOOK_DESCRIPTION_V1_STORE = BOOK_DESCRIPTION_V1 + "-store";

  private static final Logger log = LoggerFactory.getLogger(BookDescriptionTopic.class);

  @Value("${spring.kafka.properties.schema.registry.url}")
  private String schemaRegistryUrl;

  @Bean
  public NewTopic bookDescriptionCompactV1() {
    return TopicBuilder.name(BOOK_DESCRIPTION_V1).partitions(1).replicas(3).compact().build();
  }

  @Bean
  public KTable<String, BookDescription> bookDescriptionTable(StreamsBuilder streamsBuilder) {

    SpecificAvroSerde<BookDescription> avroValueSerde = new SpecificAvroSerde<>();

    Map<String, String> serdeConfig = new HashMap<>();
    serdeConfig.put("schema.registry.url", schemaRegistryUrl);
    serdeConfig.put("auto.register.schemas", "false");
    serdeConfig.put("use.latest.version", "true");

    avroValueSerde.configure(serdeConfig, false);

    KTable<String, BookDescription> table =
        streamsBuilder.table(
            BOOK_DESCRIPTION_V1,
            Consumed.with(Serdes.String(), avroValueSerde),
            Materialized.as(BOOK_DESCRIPTION_V1_STORE));

    table
        .toStream()
        .foreach(
            (key, value) -> {
              if (value == null) {
                log.info("KTable processed tombstone deletion for ISBN: {}", key);
              } else {
                log.info(
                    "KTable processed update for ISBN: {} -> Description: {}",
                    key,
                    value.getDescription());
              }
            });

    return table;
  }
}
