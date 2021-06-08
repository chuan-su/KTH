package se.sep.security.application;

import se.sep.security.domain.model.User;

import java.util.concurrent.CompletableFuture;

public interface AuthService {
  CompletableFuture<User> authenticate(String username, String password);

  User getUserFromCurrentSession();
}
