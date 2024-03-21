package ru.ac.checkpointmanager.config.payment;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.net.http.HttpClient;

/**
 * Подмена http клиента по умолчанию для работы с Wiremock (баг Wiremock, он не может распарсить тело запроса отправленного по HTTP 2.0)
 * https://github.com/wiremock/wiremock/issues/2637
 * https://github.com/wiremock/wiremock/issues/2461
 */
@TestConfiguration
public class HttpClientTestConfiguration {

    @Bean
    @Primary
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        return new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build());
    }
}
