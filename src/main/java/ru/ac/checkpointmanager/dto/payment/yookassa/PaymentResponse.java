package ru.ac.checkpointmanager.dto.payment.yookassa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.ac.checkpointmanager.dto.payment.AmountResponseDto;

import java.util.UUID;

/**
 * {
 * "id": "2d8a3d46-000f-5000-a000-187fa9cfdbc5",
 * "status": "pending",
 * "amount": {
 * "value": "10.00",
 * "currency": "RUB"
 * },
 * "description": "my order",
 * "recipient": {
 * "account_id": "353330",
 * "gateway_id": "2204238"
 * },
 * "created_at": "2024-03-18T11:46:14.789Z",
 * "confirmation": {
 * "type": "redirect",
 * "confirmation_url":
 * },
 * "test": true,
 * "paid": false,
 * "refundable": false,
 * "metadata": {
 * "order_id": "1"
 * }
 * }
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class PaymentResponse {

    UUID id;

    String status;

    Boolean paid;

    AmountResponseDto amount;

    @JsonProperty("confirmation")
    ConfirmationTypeResponseDto confirmation;

    @JsonProperty("created_at")
    String createdAt;

    String description;

    MetadataResponseDto metadata;

    @JsonProperty("recipient")
    RecipientTypeResponseDto recipient;

    @JsonProperty("payment")
    PaymentTypeResponseDto paymentType;

    boolean refundable;

    boolean test;
}
