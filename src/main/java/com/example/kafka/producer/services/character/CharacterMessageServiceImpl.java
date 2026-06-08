package com.example.kafka.producer.services.character;

import static com.example.kafka.common.topics.CharacterTopic.CHARACTER_CREATED_V1;

import com.example.generated.kafka.Character;
import com.example.generated.model.CharacterMessageRequest;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CharacterMessageServiceImpl implements CharacterMessageService {

  private static final Logger log = LoggerFactory.getLogger(CharacterMessageServiceImpl.class);

  private final KafkaTemplate<String, Character> kafkaTemplate;

  public CharacterMessageServiceImpl(KafkaTemplate<String, Character> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public void sendEmployeeMessageV1(CharacterMessageRequest request) {

    Character employee =
        Character.newBuilder()
            .setId(request.getId())
            .setName(request.getName())
            .setRole(request.getRole())
            .build();

    ProducerRecord<String, Character> characterRecord =
        new ProducerRecord<>(CHARACTER_CREATED_V1, request.getId(), employee);

    log.debug("publishing record={} to topic={}", employee, CHARACTER_CREATED_V1);
    kafkaTemplate.send(characterRecord);
  }
}
