package se.sep.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class HttpUnauthorizedException extends RuntimeException {

  public HttpUnauthorizedException(String message){
    super(message);
  }
}
