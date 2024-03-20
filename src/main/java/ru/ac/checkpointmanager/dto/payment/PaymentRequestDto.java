package ru.ac.checkpointmanager.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

/**
 * '{
 * "amount": {
 * "value": "100.00",
 * "currency": "RUB"
 * },
 * "capture": true,
 * "confirmation": {
 * "type": "redirect",
 * "return_url": "https://www.example.com/return_url"
 * },
 * "description": "Заказ №1"
 * }'
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRequestDto {

    @JsonProperty("amount")
    AmountRequestDto amount;

    Boolean capture;

    @JsonProperty("confirmation")
    ConfirmationType confirmationType;

    String description;

    MetadataRequestDto metadata;

    public PaymentRequestDto(AmountRequestDto amount, String returnUrl, String description, String orderId) {
        this.amount = amount;
        this.confirmationType = new ConfirmationType(returnUrl);
        this.description = description;
        this.capture = true;
        this.metadata = new MetadataRequestDto(orderId);
    }

    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Getter
    @ToString
    private static class MetadataRequestDto {
        @JsonProperty("order_id")
        String orderId;
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @NoArgsConstructor
    @Getter
    @ToString
    private static class ConfirmationType {
        @JsonProperty("type")
        final String type = "redirect";
        @JsonProperty("return_url")
        String returnUrl;

        public ConfirmationType(String returnUrl) {
            this.returnUrl = returnUrl;
        }
    }
}
