package ru.ac.checkpointmanager.dto.payment;

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
    BigDecimal amount;

    @NotNull
    CurrencyEnum currency;

    @NotBlank
    @Size(min = 5, max = 128)
    String comment;
}

