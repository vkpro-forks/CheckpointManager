package ru.ac.checkpointmanager.dto.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class ConfirmRegistration {

    private String fullName;
    private String email;
    private String password;
    private String verifiedToken;
}
