package com.example.kafka.producer.services.dlq;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeadLetterQueueServiceImpl implements DeadLetterQueueService {
  private static final Logger log = LoggerFactory.getLogger(DeadLetterQueueServiceImpl.class);

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public DeadLetterQueueServiceImpl(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public <K, V> void sendToDlq(ProducerRecord<K, V> record) {
    String dlqTopic = record.topic() + ".dlq";

    log.debug("sending to dlq queue={}", dlqTopic);
    kafkaTemplate.send(dlqTopic, record.key().toString(), record);
  }
}
