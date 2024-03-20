package ru.ac.checkpointmanager.configuration.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import ru.ac.checkpointmanager.dto.payment.yookassa.ErrorYooKassaResponse;
import ru.ac.checkpointmanager.exception.CriticalServerException;
import ru.ac.checkpointmanager.exception.payment.DonationException;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class YooKassaRestClientConfiguration {

    private final YooKassaProperties yookassaProperties;

    private final ObjectMapper objectMapper;

    @Bean
    @Qualifier("yooKassa")
    public RestClient yooKassaRestClient() {
        log.info("Configuring rest client for yooKassa API, base URL: {}", yookassaProperties.getUrl());
        return RestClient.builder()
                .requestInterceptor(new YooKassaRestClientInterceptor(createAuthHeader(yookassaProperties.getShopId(),
                        yookassaProperties.getSecretKey())))
                .requestFactory(clientHttpRequestFactory())
                .defaultStatusHandler(HttpStatusCode::is4xxClientError,
                        (request, response) -> {
                            ErrorYooKassaResponse errorYooKassaResponse = objectMapper
                                    .readValue(response.getBody(), ErrorYooKassaResponse.class);
                            throw new DonationException(errorYooKassaResponse.getType() + ": " +
                                    errorYooKassaResponse.getDescription());
                        })
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
                    log.warn("Error status, please check: {}", response.getStatusText());
                    throw new CriticalServerException("Something wrong YooKassa API: %s".formatted(response.getStatusText()));
                })
                .defaultStatusHandler(HttpStatusCode::is2xxSuccessful, ((request, response) ->
                        log.debug("Request successful")))
                .baseUrl(yookassaProperties.getUrl()).build();
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        return new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .build());
    }

    private String createAuthHeader(Integer shopId, String secret) {
        byte[] message = (shopId + ":" + secret)
                .getBytes(StandardCharsets.UTF_8);
        return new String(Base64.getEncoder().encode(message));
    }
}
