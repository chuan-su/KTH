package se.sep.security.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import se.sep.security.application.AuthService;
import se.sep.security.rest.dto.Login;
import se.sep.security.rest.dto.UserDto;
import se.sep.security.rest.mappers.UserMapper;

import java.util.concurrent.CompletableFuture;

@RestController
public class AuthController {

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private AuthService authService;

  @RequestMapping(value = "/api/auth", method = RequestMethod.PUT)
  public CompletableFuture<UserDto> auth(@RequestBody Login login){

    return authService.authenticate(login.username, login.password)
      .thenApply(userMapper::fromUser);
  }

}
