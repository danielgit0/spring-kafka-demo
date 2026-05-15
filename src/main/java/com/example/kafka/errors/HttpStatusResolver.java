package com.example.kafka.errors;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

class HttpStatusResolver {

  HttpStatus resolve(Exception ex) {

    if (ex instanceof ResponseStatusException rse) {
      return HttpStatus.valueOf(rse.getStatusCode().value());
    }

    if (ex instanceof HttpClientErrorException hce) {
      return HttpStatus.valueOf(hce.getStatusCode().value());
    }

    if (ex instanceof MethodArgumentTypeMismatchException
        || ex instanceof IllegalArgumentException
        || ex instanceof MethodArgumentNotValidException) {
      return HttpStatus.BAD_REQUEST;
    }

    return HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
