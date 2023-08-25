package ru.ac.checkpointmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ac.checkpointmanager.model.enums.UserRole;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "full_name")
    @NotEmpty
    @Size(min = 2, max = 100, message = "Full name have to contain between 2 and 100 characters")
    @Pattern(regexp = "(?:[А-ЯA-Z][а-яa-z]*)(?:\\s+[А-ЯA-Z][а-яa-z]*)*")
    /* проверяет, что первое и каждое следущее после пробела слово начинается с заглавной буквы (латинской или кириллицы)  */
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Email
    @NotEmpty(message = "Email should not be empty")
    private String email;

    private String password;

    @Column(name = "is_blocked")
    private Boolean isBlocked;

    @Enumerated(EnumType.STRING)
    private UserRole role;
}

