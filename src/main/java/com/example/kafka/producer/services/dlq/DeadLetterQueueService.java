package com.example.kafka.producer.services.dlq;

import org.apache.kafka.clients.producer.ProducerRecord;

public interface DeadLetterQueueService {

  <K, V> void sendToDlq(ProducerRecord<K, V> record);
}
