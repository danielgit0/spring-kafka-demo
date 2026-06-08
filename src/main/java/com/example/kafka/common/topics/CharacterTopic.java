package com.example.kafka.common.topics;

import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class CharacterTopic {

  public static final String CHARACTER_GROUP_ID_V1 = "demo.character.v1";
  public static final String CHARACTER_CREATED_V1 =
      "character.local.kafka_demo.character_created.v1";

  @Bean
  public NewTopic characterCreateV1() {
    return TopicBuilder.name(CHARACTER_CREATED_V1)
        .partitions(3)
        .replicas(3)
        .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(TimeUnit.DAYS.toMillis(7)))
        .build();
  }
}
