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

@Service
@RequiredArgsConstructor
@Slf4j
public class DonationYooKassaServiceImpl implements DonationApiService {

    private final DonationService donationService;
    private final DonationMapper mapper;
    private final RestClient restClient;

    @Override
    public DonationPerformingResponseDto makeDonation(DonationRequestDto donationRequestDto) {
        Donation savedDonation = donationService.saveUnconfirmed(donationRequestDto);
        PaymentResponse paymentResponse = restClient.post().uri("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(convertToPaymentRequest(savedDonation))
                .retrieve().body(PaymentResponse.class);
        donationService.confirm(paymentResponse);

        //send response to user
        return null;
    }

    private PaymentRequestDto convertToPaymentRequest(Donation donation) {
        return new PaymentRequestDto(new AmountRequestDto(donation.getAmount(), donation.getCurrency().name()),
                "https://checkpoint-manager.ru", donation.getComment(), donation.getId().toString());
    }
}
