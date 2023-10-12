package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    private String currentPassword;

    @NotEmpty
    @Pattern(regexp = "^(?!.*\\s).+$", message = "Field should not contain spaces")
    @Size(min = 6, max = 20)
    private String newPassword;

    private String confirmationPassword;
}
