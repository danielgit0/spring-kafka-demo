package com.example.kafka.errors;

import com.example.generated.model.ErrorMessageBody;
import com.example.generated.model.ErrorMessageEntry;
import org.springframework.http.HttpStatus;

class DefaultExceptionMapper implements ExceptionMapper {

  @Override
  public boolean supports(Exception ex) {
    return true;
  }

  @Override
  public ErrorMessageBody map(Exception ex, HttpStatus status, RequestContext ctx) {

    return new ErrorMessageBody()
        .addErrorsItem(
            new ErrorMessageEntry()
                .status(status.value())
                .title(status.getReasonPhrase())
                .detail(ErrorResponseBuilder.detail(ex))
                .cause(ErrorResponseBuilder.cause(ex))
                .meta(ErrorResponseBuilder.meta(ctx, ex.getClass().getSimpleName())));
  }
}
