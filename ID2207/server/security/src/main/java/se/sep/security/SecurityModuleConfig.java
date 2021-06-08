package se.sep.security;

import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import se.sep.common.CommonConfig;
import se.sep.security.spring.SecurityConfig;
import se.sep.security.domain.model.User;

@Configuration
@ComponentScan(basePackages = "se.sep.security")
@Import({CommonConfig.class, SecurityConfig.class})
public class SecurityModuleConfig {
  private static final String USER_COLLECTION = "user";

  @Bean
  public MongoCollection<User> userCollection(MongoDatabase mongoDatabase) {
    return mongoDatabase.getCollection(USER_COLLECTION, User.class);
  }
}
