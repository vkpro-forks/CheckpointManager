package ru.ac.checkpointmanager.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
public class UserDTO {

    @NotEmpty(message = "Name should not be empty")
    private String fullName;

    private LocalDate dateOfBirth;

    @Email
    @NotNull(message = "Email should not be empty")
    private String email;

    @NotEmpty
    private String password;
}
