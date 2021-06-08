package se.sep.hr;

import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import se.sep.common.CommonConfig;
import se.sep.hr.domain.model.RecruitmentRequest;

@Configuration
@Import(CommonConfig.class)
@ComponentScan(basePackages = "se.sep.hr")
public class HRModuleConfig {
  private static final String RECRUITMENT_REQUEST_COLLECTION = "recruitment_request";

  @Bean
  public MongoCollection<RecruitmentRequest> recruitmentRequestCollection(MongoDatabase mongoDatabase) {
    return mongoDatabase.getCollection(RECRUITMENT_REQUEST_COLLECTION , RecruitmentRequest.class);
  }
}
