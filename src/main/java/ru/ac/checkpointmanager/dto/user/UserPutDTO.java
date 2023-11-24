package ru.ac.checkpointmanager.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class UserPutDTO {

    @NotNull
    private UUID id;

    @Size(min = 2, max = 100)
    @Pattern(regexp = "(?:[А-ЯA-Z][а-яa-z]*)(?:\\s+[А-ЯA-Z][а-яa-z]*)*",
            message = "The name has to start with a capital letter and contain only Latin or Cyrillic letters.\n" +
                    "Example: \"Ivanov Ivan Jovanovich\"")
    private String fullName;

    private String mainNumber;

}