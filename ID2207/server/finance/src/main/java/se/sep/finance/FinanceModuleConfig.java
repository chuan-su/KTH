package se.sep.finance;

import com.mongodb.async.client.MongoCollection;
import com.mongodb.async.client.MongoDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import se.sep.common.CommonConfig;
import se.sep.finance.domain.model.FinancialRequest;

@Configuration
@Import(CommonConfig.class)
@ComponentScan(basePackages = "se.sep.finance")
public class FinanceModuleConfig {
  private static final String FINANCIAL_REQUEST_COLLECTION = "financial_request";

  @Bean
  public MongoCollection<FinancialRequest> financialRequestCollection(MongoDatabase mongoDatabase) {
    return mongoDatabase.getCollection(FINANCIAL_REQUEST_COLLECTION, FinancialRequest.class);
  }
}
