package se.sep.common;

import com.mongodb.async.client.MongoClient;
import com.mongodb.async.client.MongoClients;
import com.mongodb.async.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.sep.common.infrastructure.LocalDateCodec;
import se.sep.common.infrastructure.LocalDateTimeCodec;
import se.sep.common.infrastructure.SepIdCodec;

import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Configuration
public class CommonConfig {
  private static final String SEP_DATABASE = "sep";

  @Bean
  public MongoClient mongoClient() {
    MongoClient mongoClient = MongoClients.create();
    return mongoClient;
  }

  @Bean
  public MongoDatabase mongoDatabase(MongoClient mongoClient) {
    CodecRegistry pojoCodecRegistry = fromRegistries(
      fromCodecs(new SepIdCodec(), new LocalDateTimeCodec(), new LocalDateCodec()),
      MongoClients.getDefaultCodecRegistry(),
      fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    MongoDatabase mongoDatabase = mongoClient.getDatabase(SEP_DATABASE).withCodecRegistry(pojoCodecRegistry);

    return mongoDatabase;
  }
}