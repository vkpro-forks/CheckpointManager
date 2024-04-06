package ru.ac.checkpointmanager.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.ac.checkpointmanager.model.payment.CurrencyEnum;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DonationRequestDto {

    @NotNull
    @Min(50)
    @Max(100000)
    @Schema(description = "Сумма к оплате", example = "50")
    BigDecimal amount;

    @NotNull
    @Schema(description = "Валюта", example = "RUB")
    CurrencyEnum currency;

    @NotBlank
    @Size(min = 5, max = 128)
    @Schema(description = "Комментарий", example = "На кофе разработчикам")
    String comment;
}
