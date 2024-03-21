package ru.ac.checkpointmanager.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DonationPerformingResponseDto {

    AmountResponseDto amount;

    @Schema(description = "Комментарий", example = "На кофе разработчикам")
    String description;

    @Schema(description = "Ссылка для оплаты", example = "https://here-you-can-pay")
    String paymentUrl;
}
