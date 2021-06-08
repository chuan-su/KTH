package se.sep.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class HttpBadRequestException extends RuntimeException {

  public HttpBadRequestException(String message){
    super(message);
  }
}
