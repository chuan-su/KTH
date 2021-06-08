package se.sep.security.application.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import se.sep.common.exceptions.HttpUnauthorizedException;
import se.sep.security.application.AuthService;
import se.sep.security.spring.SepUserDetails;
import se.sep.security.domain.model.User;
import se.sep.security.infrastructure.UserRepository;
import se.sep.security.util.TokenGenerator;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AuthServiceImpl implements AuthService {

  private AuthenticationManager authenticationManager;

  private UserRepository userRepository;

  public AuthServiceImpl(AuthenticationManager authenticationManager, UserRepository userRepository) {
    this.authenticationManager = authenticationManager;
    this.userRepository = userRepository;
  }

  @Override
  public CompletableFuture<User> authenticate(String username, String password) {
    Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);

    return CompletableFuture.supplyAsync(() -> authenticationManager.authenticate(authentication))
      .thenApply(Authentication::getPrincipal)
      .thenCompose(obj -> {
        SepUserDetails userDetails = (SepUserDetails)obj;
        String token = null;
        try {
          token = TokenGenerator.generateToken(userDetails.getPassword());
        } catch (NoSuchAlgorithmException e) {
          throw new RuntimeException(e.getMessage());
        }
        return userRepository.updateUserAuthToken(userDetails.getUser().getSepId(), token);
      })
      .exceptionally(throwable -> {
        Throwable ex = throwable instanceof CompletionException ? throwable.getCause() : throwable;
        if (ex instanceof AuthenticationException) {
          throw new HttpUnauthorizedException(ex.getMessage());
        }
        throw new RuntimeException(ex.getMessage());
      });
  }

  @Override
  public User getUserFromCurrentSession() {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    return Optional.ofNullable(((SepUserDetails)userDetails).getUser())
      .orElseThrow(() -> new HttpUnauthorizedException("authenticated user is required"));
  }
}
