package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.ac.checkpointmanager.model.enums.PhoneNumberType;

import java.util.UUID;

@Data
public class PhoneDTO {

    private UUID id;

    @NotEmpty
    @Size(min = 6, max = 20)
    private String number;

    private PhoneNumberType type;

    private UUID userId;

    private String note;
}
