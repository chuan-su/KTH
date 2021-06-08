package se.sep.security.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import se.sep.security.application.AuthService;
import se.sep.security.application.impl.AuthServiceImpl;
import se.sep.security.infrastructure.UserRepository;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  private static final String USER_COLLECTION = "user";

  @Autowired
  private UserRepository userRepository;

  @Override
  public void configure(WebSecurity web) throws Exception{
    String[] swaggerAccesses = new String[]{
      "/v2/api-docs",
      "/swagger-resources/configuration/ui",
      "/swagger-resources",
      "/swagger-resources/configuration/security",
      "/swagger-ui.html",
      "/webjars/**",
      "/csrf"
    };

    web.ignoring()
      .antMatchers(HttpMethod.POST,"/api/user/register")
      .antMatchers(HttpMethod.PUT,"/api/auth")
      .antMatchers(swaggerAccesses);
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(daoAuthProvider())
      .authenticationProvider(preAuthAuthProvider());
  }


  @Bean
  public AuthenticationEntryPoint unauthorizedEntryPoint() {
    return (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors().and()
      .csrf().disable()
      .authorizeRequests()
      .anyRequest()
      .authenticated()
      .and()
      .addFilterBefore(new AuthTokenFilter(authenticationManager()), BasicAuthenticationFilter.class);
  }

  @Bean
  public AuthService authService() throws Exception {
    return new AuthServiceImpl(authenticationManager(), userRepository);
  }
  /*
   *  username password authentication provider
   */
  @Bean(name = "daoAuthProvider")
  public AuthenticationProvider daoAuthProvider() throws Exception {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService());
    provider.setPasswordEncoder(passwordEncoder());
    return provider;
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }

  @Bean
  public UserDetailsService userDetailsService(){ return new UserDetailServiceImpl();
  }

  /*
   *  token authentication provider
   */
  @Bean(name = "preAuthProvider")
  public AuthenticationProvider preAuthAuthProvider() throws Exception {
    PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
    provider.setPreAuthenticatedUserDetailsService(authenticationUserDetailsService());
    return provider;
  }

  @Bean
  public AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> authenticationUserDetailsService() {
    return new UserDetailServiceImpl();
  }
}
