package ru.ac.checkpointmanager.configuration.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class YooKassaRestClientInterceptor implements ClientHttpRequestInterceptor {

    private final String authHeader;

    public YooKassaRestClientInterceptor(String authHeader) {
        this.authHeader = authHeader;
    }

    @Override
    @NonNull
    public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().add("Authorization", "Basic %s".formatted(authHeader));
        request.getHeaders().add("Idempotence-Key", UUID.randomUUID().toString());
        log.debug("Auth and idempotence key header added to request: {}", request.getURI());
        return execution.execute(request, body);
    }
}
