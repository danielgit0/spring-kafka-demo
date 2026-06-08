package com.example.kafka.producer.services.employee;

import static com.example.kafka.common.topics.EmployeeTopic.EMPLOYEE_CREATED_V1;

import com.example.generated.kafka.Employee;
import com.example.generated.model.EmployeeMessageRequest;
import com.example.kafka.producer.components.ProducerCallback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EmployeeMessageServiceImpl implements EmployeeMessageService {

  private static final Logger log = LoggerFactory.getLogger(EmployeeMessageServiceImpl.class);

  private final KafkaTemplate<String, Employee> kafkaTemplate;
  private final ProducerCallback callback;

  public EmployeeMessageServiceImpl(
      KafkaTemplate<String, Employee> kafkaTemplate, ProducerCallback callback) {
    this.kafkaTemplate = kafkaTemplate;
    this.callback = callback;
  }

  @Override
  public void sendEmployeeMessageV1(EmployeeMessageRequest request) {

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
  }
}
