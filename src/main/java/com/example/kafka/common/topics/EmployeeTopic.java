package com.example.kafka.common.topics;

import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class EmployeeTopic {

  public static final String EMPLOYEE_CREATED_V1 = "employee.local.kafka_demo.employee_created.v1";

  @Bean
  public NewTopic employeeCreateV1() {
    return TopicBuilder.name(EMPLOYEE_CREATED_V1)
        .partitions(3)
        .replicas(3)
        .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(TimeUnit.DAYS.toMillis(7)))
        .build();
  }
}
