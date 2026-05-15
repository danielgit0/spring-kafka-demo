package com.example.kafka.consumer;

import static com.example.kafka.common.Topics.EMPLOYEE_CREATED_V1;

import com.example.generated.kafka.Employee;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
class Consumer {

  private static final Logger log = LoggerFactory.getLogger(Consumer.class);

  @KafkaListener(topics = {EMPLOYEE_CREATED_V1})
  public void consume(ConsumerRecord<Integer, Employee> record) {
    log.info("received = [{}] with key [{}]", record.value(), record.key());
  }
}
