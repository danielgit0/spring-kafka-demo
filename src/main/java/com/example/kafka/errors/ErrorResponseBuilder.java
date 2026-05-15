package com.example.kafka.errors;

import com.example.generated.model.ErrorMessageMeta;
import org.springframework.web.client.HttpClientErrorException;

class ErrorResponseBuilder {

  private static final String NO_MESSAGE = "No error message available";
  private static final String RESPONSE_PREFIX = " - Response: ";

  static ErrorMessageMeta meta(RequestContext ctx, String type) {
    return new ErrorMessageMeta().requestUri(ctx.path()).queryString(ctx.query()).exception(type);
  }

  static String detail(Exception ex) {
    if (ex instanceof HttpClientErrorException hce) {
      String body = hce.getResponseBodyAsString();
      if (!body.isBlank()) {
        return hce.getMessage() + RESPONSE_PREFIX + body;
      }
    }
    return ex.getMessage() != null ? ex.getMessage() : NO_MESSAGE;
  }

  static String cause(Exception ex) {
    if (ex.getCause() == null) {
      return null;
    }
    String msg = ex.getCause().getLocalizedMessage();
    return msg != null ? msg : ex.getCause().getClass().getSimpleName();
  }
}
