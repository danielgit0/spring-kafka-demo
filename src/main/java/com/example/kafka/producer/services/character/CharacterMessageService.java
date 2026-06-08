package com.example.kafka.producer.services.character;

import com.example.generated.model.CharacterMessageRequest;

public interface CharacterMessageService {

  void sendEmployeeMessageV1(CharacterMessageRequest request);
}
