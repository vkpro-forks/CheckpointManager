package ru.ac.checkpointmanager.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.configuration.payment.YooKassaClient;
import ru.ac.checkpointmanager.dto.payment.AmountRequestDto;
import ru.ac.checkpointmanager.dto.payment.AmountResponseDto;
import ru.ac.checkpointmanager.dto.payment.DonationPerformingResponseDto;
import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;
import ru.ac.checkpointmanager.dto.payment.PaymentRequestDto;
import ru.ac.checkpointmanager.dto.payment.yookassa.PaymentResponse;
import ru.ac.checkpointmanager.model.payment.Donation;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonationYooKassaServiceImpl implements DonationApiService {

    private final DonationService donationService;
    private final YooKassaClient yooKassaClient;

    @Value("${donation.redirect-url}")
    private String redirectUrl;

    @Override
    @SneakyThrows
    public DonationPerformingResponseDto makeDonation(DonationRequestDto donationRequestDto) {
        Donation savedDonation = donationService.saveUnconfirmed(donationRequestDto);
        PaymentResponse paymentResponse = yooKassaClient.doPayment(convertToPaymentRequest(savedDonation));
        Donation donation = donationService.updateWithPaymentData(paymentResponse);
        log.info("Sending confirmation url for payment");
        return new DonationPerformingResponseDto(new AmountResponseDto(donation.getAmount(), donation.getCurrency()),
                donation.getDescription(),
                paymentResponse.getConfirmation().getConfirmationUrl());
    }

    private PaymentRequestDto convertToPaymentRequest(Donation donation) {
        return new PaymentRequestDto(new AmountRequestDto(donation.getAmount(), donation.getCurrency().name()),
                redirectUrl, donation.getComment(), donation.getId().toString());
    }
}
