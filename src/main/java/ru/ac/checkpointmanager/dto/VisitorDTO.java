package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.utils.ValidationUtils;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisitorDTO {

    private UUID id;

    @NotBlank
    @Size(max = 30)
    private String name;

    @Size(min = 11, max = 20)
    @Pattern(regexp = ValidationUtils.PHONE_REGEXP)
    private String phone;

    @Size(max = 30)
    private String note;
}
