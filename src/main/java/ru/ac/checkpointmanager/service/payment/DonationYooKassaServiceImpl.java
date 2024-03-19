package ru.ac.checkpointmanager.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.ac.checkpointmanager.dto.payment.AmountRequestDto;
import ru.ac.checkpointmanager.dto.payment.DonationPerformingResponseDto;
import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;
import ru.ac.checkpointmanager.dto.payment.PaymentRequestDto;
import ru.ac.checkpointmanager.dto.payment.yookassa.PaymentResponse;
import ru.ac.checkpointmanager.mapper.payment.DonationMapper;
import ru.ac.checkpointmanager.model.payment.Donation;
import ru.ac.checkpointmanager.repository.payment.DonationRepository;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonationYooKassaServiceImpl implements DonationYooKassaService {

    private final DonationService donationService;
    private final DonationMapper mapper;
    private final RestClient restClient;

    @Override
    public DonationPerformingResponseDto makeDonation(DonationRequestDto donationRequestDto) {
        Donation savedDonation = donationService.saveUnconfirmed(donationRequestDto);
        byte[] message = (yooKassaProperties.getShopId() + ":" + yooKassaProperties.getSecretKey())
                .getBytes(StandardCharsets.UTF_8);
        String authHeader = new String(Base64.getEncoder().encode(message));
        PaymentResponse paymentResponse = restClient.post().uri("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(convertToPaymentRequest(savedDonation))
                .headers(httpHeaders -> {
                    httpHeaders.put("Authorization", List.of("Basic " + authHeader));
                    httpHeaders.put("Idempotence-Key", List.of(UUID.randomUUID().toString()));
                })
                .retrieve().body(PaymentResponse.class);
        donationService.confirm(paymentResponse);

        //update payment info in DB
        //send response to user
        return null;
    }

    private PaymentRequestDto convertToPaymentRequest(Donation donation) {
        return new PaymentRequestDto(new AmountRequestDto(donation.getAmount(), donation.getCurrency().name()),
                "https://checkpoint-manager.ru", donation.getComment(), donation.getId().toString());
    }
}
