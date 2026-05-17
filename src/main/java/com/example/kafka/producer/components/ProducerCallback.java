package com.example.kafka.producer.components;

import com.example.kafka.producer.services.dlq.DeadLetterQueueService;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
public class ProducerCallback {

  private static final Logger log = LoggerFactory.getLogger(ProducerCallback.class);

  private final DeadLetterQueueService deadLetterQueueService;

  public ProducerCallback(DeadLetterQueueService deadLetterQueueService) {
    this.deadLetterQueueService = deadLetterQueueService;
  }

  public <K, V> void onCompletion(
      SendResult<K, V> result, Throwable e, ProducerRecord<K, V> record) {
    if (e == null && result != null) {
      RecordMetadata metadata = result.getRecordMetadata();

      log.debug(
          """
          Kafka message sent successfully:
          topic={}
          partition={}
          hasOffset={}; offset={}
          hasTimestamp={}; timestamp={}
          keySize={}
          valueSize={}
          """,
          metadata.topic(),
          metadata.partition(),
          metadata.hasOffset(),
          metadata.offset(),
          metadata.hasTimestamp(),
          metadata.timestamp(),
          metadata.serializedKeySize(),
          metadata.serializedValueSize());
    } else {
      log.error("failed to publish record={}", record, e);
      deadLetterQueueService.sendToDlq(record);
    }
  }
}
