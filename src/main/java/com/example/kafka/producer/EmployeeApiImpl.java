package com.example.kafka.producer;

import static com.example.kafka.common.Topics.EMPLOYEE_CREATED_V1;

import com.example.generated.api.EmployeeApi;
import com.example.generated.kafka.Employee;
import com.example.generated.model.EmployeeMessageRequest;
import com.example.generated.model.EmployeeMessageResponse;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

@RestController
public class EmployeeApiImpl implements EmployeeApi {

  private static final Logger log = LoggerFactory.getLogger(EmployeeApiImpl.class);

  private final KafkaTemplate<String, Employee> kafkaTemplate;

  public EmployeeApiImpl(KafkaTemplate<String, Employee> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public Optional<NativeWebRequest> getRequest() {
    return EmployeeApi.super.getRequest();
  }

  @Override
  public ResponseEntity<EmployeeMessageResponse> sendEmployeeMessage(
      @RequestBody EmployeeMessageRequest request) {

    Employee employee =
        Employee.newBuilder()
            .setId(request.getId())
            .setName(request.getName())
            .setDepartment(request.getDepartment())
            .build();

    ProducerRecord<String, Employee> employeeRecord =
        new ProducerRecord<>(EMPLOYEE_CREATED_V1, request.getId(), employee);

    employeeRecord.headers().add("myTestHeader", "test".getBytes());

    CompletableFuture<SendResult<String, Employee>> result = kafkaTemplate.send(employeeRecord);

    String successMessage =
        "published to topic [%s]: [%s]\nresult: [%s]"
            .formatted(EMPLOYEE_CREATED_V1, employee, result.toString());
    log.info(successMessage);

    EmployeeMessageResponse response = new EmployeeMessageResponse();
    response.setMessage(successMessage);

    return ResponseEntity.ok(response);
  }
}
