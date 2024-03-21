package ru.ac.checkpointmanager.configuration.payment;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "yookassa")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class YooKassaProperties {

    String shopId;

    String secretKey;

    String url;
}
