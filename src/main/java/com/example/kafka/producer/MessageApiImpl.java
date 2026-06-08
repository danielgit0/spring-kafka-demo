package com.example.kafka.producer;

import com.example.generated.api.MessageApi;
import com.example.generated.model.CharacterMessageRequest;
import com.example.generated.model.EmployeeMessageRequest;
import com.example.kafka.producer.services.character.CharacterMessageService;
import com.example.kafka.producer.services.employee.EmployeeMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageApiImpl implements MessageApi {

  private final EmployeeMessageService employeeMessageService;
  private final CharacterMessageService characterMessageService;

  public MessageApiImpl(
      EmployeeMessageService employeeMessageService,
      CharacterMessageService characterMessageService) {
    this.employeeMessageService = employeeMessageService;
    this.characterMessageService = characterMessageService;
  }

  @Override
  public ResponseEntity<Void> sendEmployeeMessage(@RequestBody EmployeeMessageRequest request) {
    employeeMessageService.sendEmployeeMessageV1(request);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<Void> sendCharacterMessage(
      CharacterMessageRequest characterMessageRequest) {
    characterMessageService.sendEmployeeMessageV1(characterMessageRequest);
    return ResponseEntity.ok().build();
  }
}
