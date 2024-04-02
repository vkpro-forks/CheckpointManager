package ru.ac.checkpointmanager.dto.payment;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.ac.checkpointmanager.model.payment.CurrencyEnum;

import java.math.BigDecimal;
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record DonationRequestDto(

        @NotNull
        @Min(50)
        @Max(100000)
        @Schema(description = "Сумма к оплате", example = "50")
        BigDecimal amount,

        @NotNull
        @Schema(description = "Валюта", example = "RUB")
        CurrencyEnum currency,

        @NotBlank
        @Size(min = 5, max = 128)
        @Schema(description = "Комментарий", example = "На кофе разработчикам")
        String comment) {

    @JsonCreator //constructor need for jackson mapping
    public DonationRequestDto(@JsonProperty("amount") BigDecimal amount,
                              @JsonProperty("currency") CurrencyEnum currency,
                              @JsonProperty("comment") String comment) {
        this.amount = amount;
        this.currency = currency;
        this.comment = comment;
    }
}
