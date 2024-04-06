package ru.ac.checkpointmanager.dto.payment.yookassa;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentTypeResponseDto {

    String type;
    UUID id;
    boolean saved;
}
