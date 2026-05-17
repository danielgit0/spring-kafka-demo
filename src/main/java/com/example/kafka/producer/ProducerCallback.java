package com.example.kafka.producer;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

@Component
public class ProducerCallback {

  private static final Logger log = LoggerFactory.getLogger(ProducerCallback.class);

  public <K, V> void onCompletion(SendResult<K, V> result, Throwable e) {
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
      log.error("failed to send record", e);
    }
  }
}
