package com.example.kafka.consumer;

import static com.example.kafka.common.topics.CharacterTopic.CHARACTER_CREATED_V1;

import com.example.generated.kafka.Character;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CharacterConsumer {

  private static final Logger log = LoggerFactory.getLogger(CharacterConsumer.class);

  @KafkaListener(topics = {CHARACTER_CREATED_V1})
  public void consume(ConsumerRecord<String, Character> record) {
    log.debug("received = [{}] with key [{}]", record.value(), record.key());
    final Character character = record.value();
    final String role = character.getRole().toString();
    if (role.equals("BAD_GUY")) {
      throw new RuntimeException("Invalid character role: 'BAD_GUY' is not allowed");
    }
  }
}
