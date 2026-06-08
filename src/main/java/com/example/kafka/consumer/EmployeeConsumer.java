package com.example.kafka.consumer;

import static com.example.kafka.common.topics.EmployeeTopic.EMPLOYEE_CREATED_V1;

import com.example.generated.kafka.Employee;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EmployeeConsumer {

  private static final Logger log = LoggerFactory.getLogger(EmployeeConsumer.class);

  @KafkaListener(topics = {EMPLOYEE_CREATED_V1})
  public void consume(ConsumerRecord<String, Employee> record) {
    log.debug("received = [{}] with key [{}]", record.value(), record.key());
  }
}
