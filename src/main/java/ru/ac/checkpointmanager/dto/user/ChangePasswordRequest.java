package ru.ac.checkpointmanager.dto.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    private String currentPassword;

    @NotEmpty
    @Pattern(regexp = "^(?!.*\\s).+$", message = "Field should not contain spaces")
    @Size(min = 6, max = 20)
    private String newPassword;

    private String confirmationPassword;
}
