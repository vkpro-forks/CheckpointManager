package ru.ac.checkpointmanager.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmationRegistrationDTO {

    private String fullName;
    private String email;
    private String password;
    private String verifiedToken;
}
