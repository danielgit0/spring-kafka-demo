package com.example.kafka.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeadLetterQueueService {
  private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueService.class);

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public DeadLetterQueueService(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public <K, V> void sendToDlq(ProducerRecord<K, V> record) {
    String dlqTopic = record.topic() + ".dlq";
    String key = record.key() == null ? null : record.key().toString();

    log.debug("sending to dlq queue={}", dlqTopic);
    kafkaTemplate.send(dlqTopic, key, record);
  }
}
