package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class UserAuthDTO {

    private UUID id;

    @NotEmpty
    @Size(min = 2, max = 100)
    @Pattern(regexp = "(?:[А-ЯA-Z][а-яa-z]*)(?:\\s+[А-ЯA-Z][а-яa-z]*)*",
            message = "The name has to start with a capital letter and contain only Latin or Cyrillic letters.\n" +
                    "Example: \"Ivanov Ivan Jovanovich\"")
    private String fullName;

    @Size(min = 11, max = 20)
    private String mainNumber;

    @Email
    @NotEmpty(message = "Email should not be empty")
    private String email;

    @NotEmpty
    @Pattern(regexp = "^(?!.*\\s).+$", message = "Field should not contain spaces")
    @Size(min = 6, max = 20)
    private String password;
}
