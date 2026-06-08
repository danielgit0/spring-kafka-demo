package com.example.kafka.consumer;

import static com.example.kafka.common.topics.EmployeeTopic.EMPLOYEE_CREATED_V1;
import static com.example.kafka.common.topics.EmployeeTopic.EMPLOYER_GROUP_ID_V1;

import com.example.generated.kafka.Employee;
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
public class EmployeeConsumer {

  private static final Logger log = LoggerFactory.getLogger(EmployeeConsumer.class);

  @RetryableTopic(attempts = "2")
  @KafkaListener(
      topics = {EMPLOYEE_CREATED_V1},
      groupId = EMPLOYER_GROUP_ID_V1)
  public void consume(ConsumerRecord<String, Employee> record) {
    log.debug("received = [{}] with key [{}]", record.value(), record.key());
  }

  @DltHandler
  public void handleEmployeeV1Dlt(
      @Header(KafkaHeaders.RECEIVED_TOPIC) String dltTopic,
      ConsumerRecord<String, Employee> record) {
    log.debug(
        "Handled DLT for topic [{}] in partition [{}] with offset [{}] and record: [{}]",
        dltTopic,
        record.partition(),
        record.offset(),
        record.value());
  }
}
