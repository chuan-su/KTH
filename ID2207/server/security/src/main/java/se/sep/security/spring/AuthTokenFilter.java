package se.sep.security.spring;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

public class AuthTokenFilter extends GenericFilterBean {

  private static final String HEADER_TOKEN = "Authorization";

  private AuthenticationManager authenticationManager;

  public AuthTokenFilter(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest)request;

    try{
      String token = extractBearerToken(httpRequest).orElseThrow(() -> new MissingServletRequestParameterException(HEADER_TOKEN, "String"));
      Authentication authentication = new PreAuthenticatedAuthenticationToken(token,token);

      authentication = authenticationManager.authenticate(authentication);
      SecurityContextHolder.getContext().setAuthentication(authentication);

    } catch (MissingServletRequestParameterException | AuthenticationException e){
      SecurityContextHolder.clearContext();
    }

    chain.doFilter(request,response);
  }

  private Optional<String> extractBearerToken(HttpServletRequest httpRequest) {
    return Optional.ofNullable(httpRequest.getHeader(HEADER_TOKEN))
      .map(authHeader -> authHeader.split("Bearer"))
      .filter(authHeaders -> authHeaders.length == 2)
      .map(authHeaders -> authHeaders[1].trim());
  }
}
