package ru.ac.checkpointmanager.configuration.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.ac.checkpointmanager.dto.payment.PaymentRequestDto;
import ru.ac.checkpointmanager.dto.payment.yookassa.PaymentResponse;
import ru.ac.checkpointmanager.exception.payment.DonationException;

@Component
@Slf4j
public class YooKassaClient {

    @Qualifier("yooKassa")
    private final RestClient restClient;

    @Autowired
    public YooKassaClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public PaymentResponse doPayment(PaymentRequestDto paymentRequestDto) {
        PaymentResponse paymentResponse = restClient.post().uri("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(paymentRequestDto)
                .retrieve().body(PaymentResponse.class);
        if (paymentResponse == null) {
            throw new DonationException("No response from YooKassa API");
        }
        return paymentResponse;
    }
}
