package dh.tour.config;

import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    private final String mongoUri;

    public MongoConfig(String mongoUri) {
        this.mongoUri = mongoUri;
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(MongoClients.create(mongoUri), "nombre_de_tu_bd");
    }
}
