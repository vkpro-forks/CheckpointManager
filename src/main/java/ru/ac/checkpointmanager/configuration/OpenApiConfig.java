package ru.ac.checkpointmanager.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static ru.ac.checkpointmanager.utils.SwaggerConstants.SWAGGER_DESCRIPTION_MESSAGE;


@Configuration
public class OpenApiConfig {

    @Value("${app.version}")
    private String appVersion;

    @Bean
    @Profile("dev")
    public OpenAPI openAPIDev() {
        return new OpenAPI()
                .addServersItem(new Server().url("http://localhost:8080"))
                .info(new Info().title("Checkpoint Manager")
                        .description(SWAGGER_DESCRIPTION_MESSAGE)
                        .version(appVersion));
    }

    @Bean
    @Profile("prod")
    public OpenAPI openAPIProd() {
        return new OpenAPI()
                .addServersItem(new Server().url("https://checkpoint-manager.ru/api"))
                .info(new Info().title("Checkpoint Manager")
                        .description(SWAGGER_DESCRIPTION_MESSAGE)
                        .version(appVersion));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("божественная апишечка - Enjoy using our API") //TODO потом сменить
                .pathsToMatch("/api/**")
                .build();
    }
}
