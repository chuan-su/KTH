package se.sep.security.application;

import se.sep.security.domain.model.Team;
import se.sep.security.domain.model.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserService {
  CompletableFuture<User> register(User user);
  CompletableFuture<List<User>> listUsersInTeam(Team team);
}
