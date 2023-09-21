package ru.ac.checkpointmanager.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {

    @Value("${app.version}")
    private String appVersion;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("https://checkpoint-manager.ru"))
                .info(new Info().title("Checkpoint Manager")
                        .description("Наши бэкэндеры лучшие! Не забудем и о тестерах и фронтах конечно!")
                        .version(appVersion));
    }
}
