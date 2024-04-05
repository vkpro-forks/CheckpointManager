package ru.ac.checkpointmanager.util;

import lombok.experimental.UtilityClass;
import ru.ac.checkpointmanager.dto.payment.AmountRequestDto;
import ru.ac.checkpointmanager.dto.payment.AmountResponseDto;
import ru.ac.checkpointmanager.dto.payment.DonationPerformingResponseDto;
import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;
import ru.ac.checkpointmanager.dto.payment.PaymentRequestDto;
import ru.ac.checkpointmanager.dto.payment.yookassa.ConfirmationTypeResponseDto;
import ru.ac.checkpointmanager.dto.payment.yookassa.ErrorYooKassaResponse;
import ru.ac.checkpointmanager.dto.payment.yookassa.MetadataResponseDto;
import ru.ac.checkpointmanager.dto.payment.yookassa.PaymentResponse;
import ru.ac.checkpointmanager.dto.payment.yookassa.RecipientTypeResponseDto;
import ru.ac.checkpointmanager.model.payment.CurrencyEnum;
import ru.ac.checkpointmanager.model.payment.Donation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@UtilityClass
public class YooKassaTestData {

    public static MatcherFactory.Matcher<Donation> DONATION_MATCHER = MatcherFactory.usingIgnoringFieldsComparator("user");

    public static final String DESCRIPTION = "for coffee";

    public static final String COMMENT = DESCRIPTION;

    public static final UUID DONATION_ID = UUID.randomUUID();

    public static final BigDecimal AMOUNT = BigDecimal.valueOf(100);

    public static final ZonedDateTime CREATED_AT = ZonedDateTime.of(LocalDateTime.of(2024, 3, 18, 10, 0, 0), ZoneId.of("UTC"));


    public static final String RETURN_URL = "https://checkpoint-manager.ru/thank-you";

    public static final String PAYMENT_URL = "https://pay-me-please";

    public static PaymentResponse paymentResponse =
            PaymentResponse.builder()
                    .id(UUID.randomUUID())
                    .test(true)
                    .description(DESCRIPTION)
                    .amount(new AmountResponseDto(AMOUNT, CurrencyEnum.RUB))
                    .status("pending")
                    .recipient(new RecipientTypeResponseDto("id", "id"))
                    .createdAt(CREATED_AT.toString())
                    .refundable(false)
                    .confirmation(new ConfirmationTypeResponseDto("redirect", PAYMENT_URL))
                    .paid(false)
                    .metadata(new MetadataResponseDto(UUID.randomUUID().toString()))
                    .build();

    public static Donation preSendDonation = new Donation(DONATION_ID, AMOUNT, CurrencyEnum.RUB, COMMENT);

    public static DonationRequestDto donationRequestDto = new DonationRequestDto(AMOUNT, CurrencyEnum.RUB,
            COMMENT);

    public static Donation preFilledDonation = new Donation(AMOUNT, CurrencyEnum.RUB, COMMENT);
    public static PaymentRequestDto paymentRequest = new PaymentRequestDto(
            new AmountRequestDto(AMOUNT, CurrencyEnum.RUB.name()), RETURN_URL, COMMENT, DONATION_ID.toString());

    public static DonationPerformingResponseDto donationPerforming = new DonationPerformingResponseDto(
            new AmountResponseDto(AMOUNT, CurrencyEnum.RUB), DESCRIPTION, PAYMENT_URL);

    public static ErrorYooKassaResponse yooKassaBadRequestError = new ErrorYooKassaResponse("error", "errorId",
            "errorCode", "description", "parameter");

    public static Donation updatedDonation = new Donation(DONATION_ID, AMOUNT, CurrencyEnum.RUB,
            COMMENT, paymentResponse.getPaid(), paymentResponse.getStatus(), CREATED_AT, DESCRIPTION, paymentResponse.getId());

    public static Donation newDonationAfterMapping = new Donation(UUID.fromString(paymentResponse.getMetadata().getOrderId()),
            AMOUNT, CurrencyEnum.RUB, DESCRIPTION, paymentResponse.getPaid(),
            paymentResponse.getStatus(), CREATED_AT, DESCRIPTION, paymentResponse.getId());
}
