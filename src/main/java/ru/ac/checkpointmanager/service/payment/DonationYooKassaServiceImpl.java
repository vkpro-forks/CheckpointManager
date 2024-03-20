package ru.ac.checkpointmanager.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.configuration.payment.YooKassaClient;
import ru.ac.checkpointmanager.dto.payment.AmountRequestDto;
import ru.ac.checkpointmanager.dto.payment.AmountResponseDto;
import ru.ac.checkpointmanager.dto.payment.DonationPerformingResponseDto;
import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;
import ru.ac.checkpointmanager.dto.payment.PaymentRequestDto;
import ru.ac.checkpointmanager.dto.payment.yookassa.PaymentResponse;
import ru.ac.checkpointmanager.model.payment.Donation;

/**
 * Сервис отправляет запрос с данными платежа на API ЮКасса
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DonationYooKassaServiceImpl implements DonationApiService {

    private final DonationService donationService;
    private final YooKassaClient yooKassaClient;

    @Value("${donation.return-url}")
    private String returnUrl;

    /**
     * Платеж предварительно сохраняется в бд с идентификационным номером, отправляется запрос на API,
     * после получения ответа от API -> в бд обновляются и обогащаются данные о платеже
     * <br>
     * Возвращается ответ с ссылкой на оплату
     *
     * @param donationRequestDto {@link DonationRequestDto} запрос с данными на оплату
     * @return {@link DonationPerformingResponseDto} ответ с данными оплаты и ссылкой
     */
    @Override
    @SneakyThrows
    @NonNull
    public DonationPerformingResponseDto makeDonation(@NonNull DonationRequestDto donationRequestDto) {
        Donation savedDonation = donationService.saveUnconfirmed(donationRequestDto);
        PaymentResponse paymentResponse = yooKassaClient.doPayment(convertToPaymentRequest(savedDonation));
        Donation donation = donationService.updateWithPaymentData(paymentResponse);
        log.info("Sending confirmation url for payment");
        return new DonationPerformingResponseDto(new AmountResponseDto(donation.getAmount(), donation.getCurrency()),
                donation.getDescription(),
                paymentResponse.getConfirmation().getConfirmationUrl());
    }

    /**
     * Маппит данные оплаты в запрос на API
     */
    @NonNull
    private PaymentRequestDto convertToPaymentRequest(@NonNull Donation donation) {
        return new PaymentRequestDto(new AmountRequestDto(donation.getAmount(), donation.getCurrency().name()),
                returnUrl, donation.getComment(), donation.getId().toString());
    }
}
