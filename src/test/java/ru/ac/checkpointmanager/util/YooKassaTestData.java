package ru.ac.checkpointmanager.util;

import lombok.experimental.UtilityClass;
import ru.ac.checkpointmanager.dto.payment.AmountResponseDto;
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

    public static MatcherFactory.Matcher<Donation> DONATION_MATHER = MatcherFactory.usingIgnoringFieldsComparator("user");

    public static final String DESCRIPTION = "payment";

    public static final String COMMENT = "for coffee";

    public static final UUID DONATION_ID = UUID.randomUUID();

    public static final ZonedDateTime CREATED_AT = ZonedDateTime.of(LocalDateTime.of(2024, 3, 18, 10, 0, 0), ZoneId.of("UTC"));

    public static Donation preDonation = new Donation(DONATION_ID, BigDecimal.TEN, CurrencyEnum.RUB, COMMENT);


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
                    .confirmation(new ConfirmationTypeResponseDto("redirect",
                            "https://checkpoint-manager.ru"))
                    .paid(false)
                    .metadata(new MetadataResponseDto(UUID.randomUUID().toString()))
                    .build();

    public static Donation updatedDonation = new Donation(DONATION_ID, BigDecimal.TEN, CurrencyEnum.RUB,
            COMMENT, PAYMENT_RESPONSE.getPaid(), PAYMENT_RESPONSE.getStatus(), CREATED_AT, DESCRIPTION, PAYMENT_RESPONSE.getId());

    public static Donation newDonationAfterMapping = new Donation(UUID.fromString(PAYMENT_RESPONSE.getMetadata().getOrderId()),
            BigDecimal.TEN, CurrencyEnum.RUB, DESCRIPTION, PAYMENT_RESPONSE.getPaid(),
            PAYMENT_RESPONSE.getStatus(), CREATED_AT, DESCRIPTION, PAYMENT_RESPONSE.getId());
}
