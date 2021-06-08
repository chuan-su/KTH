package se.sep.security.application.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import se.sep.common.domain.model.SepId;
import se.sep.common.exceptions.HttpBadRequestException;
import se.sep.security.application.UserService;
import se.sep.security.domain.model.Team;
import se.sep.security.domain.model.User;
import se.sep.security.infrastructure.UserRepository;
import se.sep.security.util.TokenGenerator;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public class UserServiceImpl implements UserService  {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  @Override
  public CompletableFuture<User> register(User user) {
    return userRepository.findByUserName(user.getUsername())
      .thenCompose(foundUser -> {
        if (Objects.nonNull(foundUser)) {
          throw new HttpBadRequestException("user name" + user.getUsername() + " already taken");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        try {
          user.setAuthToken(TokenGenerator.generateToken(user.getPassword()));
        } catch (NoSuchAlgorithmException e) {
          throw new RuntimeException(e.getMessage());
        }
        return userRepository.createUser(user);
      });
  }

  @Override
  public CompletableFuture<List<User>> listUsersInTeam(Team team) {
    return userRepository.findByTeam(team);
  }
}
