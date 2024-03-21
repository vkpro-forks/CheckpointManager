package ru.ac.checkpointmanager.dto.payment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.ac.checkpointmanager.model.payment.CurrencyEnum;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AmountResponseDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal value;

    CurrencyEnum currency;
}
