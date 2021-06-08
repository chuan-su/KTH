package se.sep.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.*;

import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
      .allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH")
      .allowCredentials(true);
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    final CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));

    /*
     * setAllowCredentials(true) is important, otherwise:
     * The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when
     * the request's credentials mode is 'include'.
     */
    configuration.setAllowCredentials(true);

    /*
     * setAllowedHeaders is important! Without it, OPTIONS preflight request will fail with 403 Invalid CORS request
     */
    configuration.setAllowedHeaders(Arrays.asList("Authorization","Bearer","Cache-Control", "Content-Type","Accept"));

    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
  }
}
