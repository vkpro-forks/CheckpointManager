package ru.ac.checkpointmanager.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.ac.checkpointmanager.model.enums.PhoneNumberType;

import java.util.UUID;

@Data
public class PhoneNumberDTO {

    @NotEmpty
    @Size(min = 6, max = 11)
    @Pattern(regexp = " ^\\d+$", message = "The number has to contain only numbers from 0 to 9\n" +
            "Example: \"79998885566\"")
    private String number;

    @Enumerated(EnumType.STRING)
    private PhoneNumberType type;

    @NotEmpty
    private UUID userId;

    private String note;

}
