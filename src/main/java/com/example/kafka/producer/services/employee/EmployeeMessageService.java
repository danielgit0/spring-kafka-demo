package com.example.kafka.producer.services.employee;

import com.example.generated.model.EmployeeMessageRequest;

public interface EmployeeMessageService {

  void sendEmployeeMessageV1(EmployeeMessageRequest request);
}
