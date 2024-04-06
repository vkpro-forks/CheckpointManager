package ru.ac.checkpointmanager.dto.payment.yookassa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecipientTypeResponseDto {

    @JsonProperty("account_id")
    String accountId;

    @JsonProperty("gateway_id")
    String gatewayId;
}
