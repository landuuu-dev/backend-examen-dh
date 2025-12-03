package dh.tour.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {

    @Bean
    public String mongoUri() {
        return System.getenv("MONGO_URI");
    }

    @Bean
    public String frontendUrl() {
        return System.getenv("FRONTEND_URL");
    }
}

