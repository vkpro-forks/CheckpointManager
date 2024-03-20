package ru.ac.checkpointmanager.util;

import lombok.experimental.UtilityClass;
import ru.ac.checkpointmanager.dto.payment.AmountRequestDto;
import ru.ac.checkpointmanager.dto.payment.AmountResponseDto;
import ru.ac.checkpointmanager.dto.payment.DonationPerformingResponseDto;
import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;
import ru.ac.checkpointmanager.dto.payment.PaymentRequestDto;
import ru.ac.checkpointmanager.dto.payment.yookassa.ConfirmationTypeResponseDto;
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

    public static final String DESCRIPTION = "payment";

    public static final String COMMENT = "for coffee";

    public static final UUID DONATION_ID = UUID.randomUUID();

    public static final ZonedDateTime CREATED_AT = ZonedDateTime.of(LocalDateTime.of(2024, 3, 18, 10, 0, 0), ZoneId.of("UTC"));

    public static Donation preSendDonation = new Donation(DONATION_ID, BigDecimal.TEN, CurrencyEnum.RUB, COMMENT);

    public static DonationRequestDto donationRequestDto = new DonationRequestDto(BigDecimal.TEN, CurrencyEnum.RUB,
            COMMENT);

    public static Donation preFilledDonation = new Donation(BigDecimal.TEN, CurrencyEnum.RUB, COMMENT);

    public static final String RETURN_URL = "https://checkpoint-manager.ru/thank-you";

    public static PaymentRequestDto PAYMENT_REQUEST = new PaymentRequestDto(
            new AmountRequestDto(BigDecimal.TEN, CurrencyEnum.RUB.name()), RETURN_URL, COMMENT, DONATION_ID.toString());

    public static DonationPerformingResponseDto DONATION_PERFORMING = new DonationPerformingResponseDto(
            new AmountResponseDto(BigDecimal.TEN, CurrencyEnum.RUB), DESCRIPTION, RETURN_URL);

    public static final PaymentResponse PAYMENT_RESPONSE =
            PaymentResponse.builder()
                    .id(UUID.randomUUID())
                    .test(true)
                    .description(DESCRIPTION)
                    .amount(new AmountResponseDto(BigDecimal.TEN, CurrencyEnum.RUB))
                    .status("pending")
                    .recipient(new RecipientTypeResponseDto("id", "id"))
                    .createdAt(CREATED_AT.toString())
                    .refundable(false)
                    .confirmation(new ConfirmationTypeResponseDto("redirect", RETURN_URL))
                    .paid(false)
                    .metadata(new MetadataResponseDto(UUID.randomUUID().toString()))
                    .build();

    public static Donation updatedDonation = new Donation(DONATION_ID, BigDecimal.TEN, CurrencyEnum.RUB,
            COMMENT, PAYMENT_RESPONSE.getPaid(), PAYMENT_RESPONSE.getStatus(), CREATED_AT, DESCRIPTION, PAYMENT_RESPONSE.getId());

    public static Donation newDonationAfterMapping = new Donation(UUID.fromString(PAYMENT_RESPONSE.getMetadata().getOrderId()),
            BigDecimal.TEN, CurrencyEnum.RUB, DESCRIPTION, PAYMENT_RESPONSE.getPaid(),
            PAYMENT_RESPONSE.getStatus(), CREATED_AT, DESCRIPTION, PAYMENT_RESPONSE.getId());
}
