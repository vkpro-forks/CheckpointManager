package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.model.enums.PhoneNumberType;
import ru.ac.checkpointmanager.utils.ValidationUtils;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhoneDTO {

    private UUID id;

    @NotEmpty
    @Size(min = 11, max = 20)
    @Pattern(regexp = ValidationUtils.PHONE_REGEXP,
            message = "должно соответствовать формату номера телефона")
    private String number;

    @NotNull
    private PhoneNumberType type;

    @NotNull
    private UUID userId;

    private String note;
}
