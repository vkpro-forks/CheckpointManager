package ru.ac.checkpointmanager.dto.payment.yookassa;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * {
 * "type": "error",
 * "id": "4779eee6-1607-4ca2-bba8-a42511269cb6",
 * "code": "invalid_request",
 * "description": "Missing payment data (payment_method_data, payment_method_id, or payment_token***",
 * "parameter": "confirmation"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ErrorYooKassaResponse {

    String type;
    String id;
    String code;
    String description;
    String parameter;
}