package ru.ac.checkpointmanager.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "cors")
@Data
public class CorsProperties {

    private Boolean allowCredentials;

    List<String> allowedOrigins;

    List<String> allowedMethods;

    List<String> allowedHeaders;

}

