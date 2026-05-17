package com.example.kafka.producer;

import static com.example.kafka.common.Topics.EMPLOYEE_CREATED_V1;

import com.example.generated.api.MessageApi;
import com.example.generated.kafka.Employee;
import com.example.generated.model.EmployeeMessageRequest;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageApiImpl implements MessageApi {

  private static final Logger log = LoggerFactory.getLogger(MessageApiImpl.class);

  private final KafkaTemplate<String, Employee> kafkaTemplate;
  private final ProducerCallback callback;

  public MessageApiImpl(KafkaTemplate<String, Employee> kafkaTemplate, ProducerCallback callback) {
    this.kafkaTemplate = kafkaTemplate;
    this.callback = callback;
  }

  @Override
  public ResponseEntity<Void> sendEmployeeMessage(@RequestBody EmployeeMessageRequest request) {

    Employee employee =
        Employee.newBuilder()
            .setId(request.getId())
            .setName(request.getName())
            .setDepartment(request.getDepartment())
            .build();

    ProducerRecord<String, Employee> employeeRecord =
        new ProducerRecord<>(EMPLOYEE_CREATED_V1, request.getId(), employee);

    employeeRecord.headers().add("myTestHeader", "test".getBytes());

    log.debug("publishing record={} to topic={}", employee, EMPLOYEE_CREATED_V1);
    kafkaTemplate
        .send(employeeRecord)
        .whenComplete((result, ex) -> callback.onCompletion(result, ex, employeeRecord));

    return ResponseEntity.ok().build();
  }
}
