package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDate;

@Getter
@Setter
public class UserDTO {

    @NotEmpty
    @Size(min = 2, max = 100, message = "Full name have to contain between 2 and 100 characters")
    @Pattern(regexp = "(?:[А-ЯA-Z][а-яa-z]*)(?:\\s+[А-ЯA-Z][а-яa-z]*)*")
    private String fullName;

    private LocalDate dateOfBirth;

    @Email
    @NotEmpty(message = "Email should not be empty")
    private String email;

    @NotEmpty
    private String password;
}
