package se.sep.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RestController;
import se.sep.epr.EprModuleConfig;
import se.sep.finance.FinanceModuleConfig;
import se.sep.hr.HRModuleConfig;
import se.sep.production.ProductionModuleConfig;
import se.sep.security.SecurityModuleConfig;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@Import({
  SecurityModuleConfig.class,
  FinanceModuleConfig.class,
  ProductionModuleConfig.class,
  EprModuleConfig.class,
  HRModuleConfig.class
})
@EnableSwagger2
public class SwaggerConfig {
  @Bean
  public Docket api() {
    AuthorizationScope[] authScopes = new AuthorizationScope[1];
    authScopes[0] = new AuthorizationScope("global", "accessEverything");
    SecurityReference securityReference = new SecurityReference("Bearer", authScopes);

    SecurityContext securityContext
      = SecurityContext.builder().securityReferences(Arrays.asList(securityReference)).forPaths(PathSelectors.any()).build();

    return new Docket(DocumentationType.SWAGGER_2)
      .apiInfo(new ApiInfoBuilder().title("SEP").version("1.0").build())
      .select()
      .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
      .paths(PathSelectors.any())
      .build()
      .securitySchemes(Collections.singletonList(new ApiKey("Bearer", "Authorization", "header")))
      .securityContexts(Arrays.asList(securityContext));
  }
}
