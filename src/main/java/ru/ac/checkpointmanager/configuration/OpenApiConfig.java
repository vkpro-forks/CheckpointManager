package ru.ac.checkpointmanager.configuration;

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

    private static final String CHECKPOINT_API_V_1 = "Checkpoint API: v1";
    private static final String API_V_1 = "/api/v1/**";

    @Value("${app.version}")
    private String appVersion;

    private static final Info CHP_INFO = new Info().title("Checkpoint Manager").description(SWAGGER_DESCRIPTION_MESSAGE);

    @Bean
    @Profile("prod")
    public GroupedOpenApi prodApi() {
        return GroupedOpenApi.builder()
                .addOpenApiCustomizer(openApi ->
                        openApi.addServersItem(new Server().url("https://checkpoint-manager.ru"))
                                .info(CHP_INFO.version(appVersion)))
                .group(CHECKPOINT_API_V_1)
                .pathsToMatch(API_V_1)
                .build();
    }

    @Bean
    @Profile({"dev", "test"})
    public GroupedOpenApi devApi() {
        return GroupedOpenApi.builder().addOpenApiCustomizer(openApi ->
                        openApi.addServersItem(new Server().url("http://localhost:8080"))
                                .info(CHP_INFO.version(appVersion)))
                .group("Божественная апишечка - Enjoy using our API")
                .pathsToMatch(API_V_1)
                .build();
    }
}
