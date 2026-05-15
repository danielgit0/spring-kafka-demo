package com.example.kafka.errors;

import com.example.generated.model.ErrorMessageBody;
import org.springframework.http.HttpStatus;

public interface ExceptionMapper {

  boolean supports(Exception ex);

  ErrorMessageBody map(Exception ex, HttpStatus status, RequestContext ctx);
}
