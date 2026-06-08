package com.example.kafka.consumer;

import static com.example.kafka.common.topics.CharacterTopic.CHARACTER_CREATED_V1;
import static com.example.kafka.common.topics.CharacterTopic.CHARACTER_GROUP_ID_V1;

import com.example.generated.kafka.Character;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class CharacterConsumer {

  private static final Logger log = LoggerFactory.getLogger(CharacterConsumer.class);

  @RetryableTopic(attempts = "4")
  @KafkaListener(
      topics = {CHARACTER_CREATED_V1},
      groupId = CHARACTER_GROUP_ID_V1)
  public void consume(ConsumerRecord<String, Character> record) {
    log.debug("received = [{}] with key [{}]", record.value(), record.key());
    final Character character = record.value();
    final String role = character.getRole().toString();
    if (role.equals("BAD_GUY")) {
      throw new RuntimeException("Invalid character role: 'BAD_GUY' is not allowed");
    }
  }

  @DltHandler
  public void handleCharacterV1Dlt(
      @Header(KafkaHeaders.RECEIVED_TOPIC) String dltTopic,
      ConsumerRecord<String, Character> record) {
    log.debug(
        "Handled DLT for topic [{}] in partition [{}] with offset [{}] and record: [{}]",
        dltTopic,
        record.partition(),
        record.offset(),
        record.value());
  }
}
