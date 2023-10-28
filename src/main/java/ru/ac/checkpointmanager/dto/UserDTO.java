package ru.ac.checkpointmanager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.ac.checkpointmanager.model.enums.Role;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class UserDTO {

    private UUID id;

    @NotEmpty
    @Size(min = 2, max = 100)
    @Pattern(regexp = "(?:[А-ЯA-Z][а-яa-z]*)(?:\\s+[А-ЯA-Z][а-яa-z]*)*",
            message = "The name has to start with a capital letter and contain only Latin or Cyrillic letters.\n" +
                    "Example: \"Ivanov Ivan Jovanovich\"")
    private String fullName;

    private LocalDate dateOfBirth;

    @NotEmpty
    @Size(min = 11, max = 20)
    private String mainNumber;

    @Email
    @NotEmpty(message = "Email should not be empty")
    private String email;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean isBlocked;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Role role;
}

