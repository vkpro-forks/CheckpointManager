package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ChangeEmailRequest {

    private String currentEmail;

    @Email
    private String newEmail;
}