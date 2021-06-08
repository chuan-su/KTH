package se.sep.security.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import se.sep.security.application.UserService;
import se.sep.security.domain.model.Team;
import se.sep.security.domain.model.User;
import se.sep.security.rest.dto.NewUser;
import se.sep.security.rest.dto.UserDto;
import se.sep.security.rest.mappers.UserMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

@RestController
public class UserController {
  @Autowired
  private UserMapper userMapper;

  @Autowired
  private UserService userService;

  @RequestMapping(value = "/api/user/register", method = RequestMethod.POST)
  public CompletableFuture<UserDto> registerUser(@RequestBody NewUser newUser) {
    User user = userMapper.toUser(newUser);

    return userService.register(user)
      .thenApply(userMapper::fromUser);
  }

  @RequestMapping(value = "/api/users/{team}", method = RequestMethod.GET)
  public CompletableFuture<List<UserDto>> findUsersByTeam(@PathVariable Team team) {

    return userService.listUsersInTeam(team)
      .thenApply(users -> users.stream().map(userMapper::fromUser).collect(toList()));
  }
}
