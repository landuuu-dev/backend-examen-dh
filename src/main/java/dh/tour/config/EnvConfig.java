package dh.tour.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {

    private final Dotenv dotenv = Dotenv.load(); // Lee el archivo .env

    @Bean
    public String mongoUri() {
        return dotenv.get("MONGO_URI"); // Devuelve la URI de Mongo
    }

    @Bean
    public String frontendUrl() {
        return dotenv.get("FRONTEND_URL"); // Devuelve la URL del frontend
    }
}
