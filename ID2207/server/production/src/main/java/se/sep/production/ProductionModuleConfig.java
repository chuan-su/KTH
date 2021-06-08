package se.sep.production;

import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import se.sep.common.CommonConfig;
import se.sep.production.domain.model.Project;

@Configuration
@Import(CommonConfig.class)
@ComponentScan(basePackages = "se.sep.production")
public class ProductionModuleConfig {
  private static final String PROJECT_COLLECTION = "project";

  @Bean
  public MongoCollection<Project> projectCollection(MongoDatabase mongoDatabase) {
    return mongoDatabase.getCollection(PROJECT_COLLECTION, Project.class);
  }
}
