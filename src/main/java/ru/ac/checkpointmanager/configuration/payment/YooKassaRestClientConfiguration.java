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

import java.net.http.HttpClient;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class YooKassaRestClientConfiguration {

    private final YooKassaProperties yookassaProperties;

    private final ObjectMapper objectMapper;

    @Bean
    @Qualifier("yookassa")
    public RestClient yooKassaRestClient() {
        log.info("Configuring rest client for yooKassa API, base URL: {}", yookassaProperties.getUrl());
        return RestClient.builder()
                .requestFactory(clientHttpRequestFactory())
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) ->
                        log.warn("Error status: {}, message: {}", response.getStatusText(),
                                objectMapper.readValue(response.getBody(), ErrorYooKassaResponse.class)))
                .defaultStatusHandler(HttpStatusCode::is5xxServerError, (request, response) -> {
                    log.warn("Error status, please check: {}", response.getStatusText());
                    throw new CriticalServerException("Something wrong YooKassa API");
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
}
