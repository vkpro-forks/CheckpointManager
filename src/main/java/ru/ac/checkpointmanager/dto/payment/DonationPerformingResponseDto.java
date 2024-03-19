package ru.ac.checkpointmanager.dto.payment;

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

    String description;

    String paymentUrl;
}
