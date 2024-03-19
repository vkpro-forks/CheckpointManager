package ru.ac.checkpointmanager.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRequestDto {

    AmountRequestDto amountRequestDto;

    Boolean capture;

    @JsonProperty("confirmation")
    ConfirmationType confirmationType;

    String description;

    MetadataRequestDto metadata;

    public PaymentRequestDto(AmountRequestDto amount, String returnUrl, String description, String orderId) {
        this.amountRequestDto = amount;
        this.confirmationType = new ConfirmationType(returnUrl);
        this.description = description;
        this.capture = true;
        this.metadata = new MetadataRequestDto(orderId);
    }

    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static class MetadataRequestDto {
        @JsonProperty("order_id")
        String orderId;
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static class ConfirmationType {
        final String type = "redirect";
        @JsonProperty("return_url")
        String returnUrl;

        public ConfirmationType(String returnUrl) {
            this.returnUrl = returnUrl;
        }
    }
}
