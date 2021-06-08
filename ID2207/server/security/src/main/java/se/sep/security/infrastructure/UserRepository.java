package se.sep.security.infrastructure;

import se.sep.common.domain.model.SepId;
import se.sep.security.domain.model.Team;
import se.sep.security.domain.model.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserRepository {

  CompletableFuture<User> findByUserName(String username);

  CompletableFuture<List<User>> findByTeam(Team team);

  CompletableFuture<User> findByAuthToken(String token);

  CompletableFuture<User> updateUserAuthToken(SepId userId, String token);

  CompletableFuture<User> createUser(User user);
}
