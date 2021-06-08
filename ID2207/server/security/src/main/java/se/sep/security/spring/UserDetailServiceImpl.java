package se.sep.security.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import se.sep.security.domain.model.User;
import se.sep.security.infrastructure.UserRepository;

import java.util.Objects;

public class UserDetailServiceImpl implements UserDetailsService, AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
  @Autowired
  private UserRepository userRepository;

  @Override
  public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
    String authToken = (String)token.getPrincipal();
    User user = userRepository.findByAuthToken(authToken).join();
    if (Objects.isNull(user)) {
      throw new UsernameNotFoundException("Invalid auth token");
    }
    return new SepUserDetails(user);
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUserName(username).join();
    if (Objects.isNull(user) || Objects.isNull(user.getSepId())) {
      throw new UsernameNotFoundException("User " + username + " not exist");
    }
    return new SepUserDetails(user);
  }
}
