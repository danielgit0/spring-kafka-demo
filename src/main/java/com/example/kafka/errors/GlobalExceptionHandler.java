package com.example.kafka.errors;

import com.example.generated.model.ErrorMessageBody;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private final List<ExceptionMapper> mappers;
  private final HttpStatusResolver statusResolver = new HttpStatusResolver();

  public GlobalExceptionHandler(List<ExceptionMapper> mappers) {
    this.mappers = mappers;
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorMessageBody> handle(Exception ex, HttpServletRequest request) {

    RequestContext ctx = RequestContext.from(request);
    HttpStatus status = statusResolver.resolve(ex);

    log(ex, status, ctx);

    ExceptionMapper mapper = resolveMapper(ex);

    ErrorMessageBody body = mapper.map(ex, status, ctx);

    return ResponseEntity.status(status).body(body);
  }

  private ExceptionMapper resolveMapper(Exception ex) {
    return mappers.stream()
        .filter(m -> m.supports(ex))
        .findFirst()
        .orElse(new DefaultExceptionMapper());
  }

  private void log(Exception ex, HttpStatus status, RequestContext ctx) {
    if (status.is5xxServerError()) {
      log.error("Request {} failed", ctx.fullPath(), ex);
    } else {
      log.warn("Request {} failed", ctx.fullPath(), ex);
    }
  }
}
