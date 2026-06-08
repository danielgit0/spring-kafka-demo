package com.example.kafka;

import static com.example.kafka.common.topics.CharacterTopic.CHARACTER_CREATED_V1;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.generated.kafka.Character;
import com.example.kafka.consumer.CharacterConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {"spring.kafka.properties.schema.registry.url=mock://test-registry"})
public class CharacterConsumerTest {

  @Autowired private KafkaTemplate<String, Character> kafkaTemplate;

  @MockitoSpyBean private CharacterConsumer characterConsumer;

  @Test
  void testConsumeSuccessful() {
    Character character =
        Character.newBuilder().setId("1").setName("Luke Skywalker").setRole("HERO").build();

    kafkaTemplate.send(CHARACTER_CREATED_V1, "1", character);

    verify(characterConsumer, timeout(10000)).consume(any());
    verify(characterConsumer, times(1)).consume(any());
  }

  @Test
  void testConsumeDltFlow() {
    Character character =
        Character.newBuilder().setId("2").setName("Darth Vader").setRole("BAD_GUY").build();

    kafkaTemplate.send(CHARACTER_CREATED_V1, "2", character);

    verify(characterConsumer, times(4)).consume(any());
  }
}
