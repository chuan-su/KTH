package se.sep.epr;

import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import se.sep.common.CommonConfig;
import se.sep.epr.domain.model.Epr;

@Configuration
@Import(CommonConfig.class)
@ComponentScan(basePackages = "se.sep.epr")
public class EprModuleConfig {
  private static final String EPR = "epr";

  @Bean
  public MongoCollection<Epr> eprCollection(MongoDatabase mongoDatabase) {
    return mongoDatabase.getCollection(EPR, Epr.class);
  }
}
