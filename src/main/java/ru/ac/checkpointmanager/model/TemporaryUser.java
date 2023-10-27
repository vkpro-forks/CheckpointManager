package ru.ac.checkpointmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Класс TemporaryUser представляет временный объект пользователях, содержит всю необходимую информацию
 * для создания основного пользователя приложения.
 *
 * @author fifimova
 * @see User
 */

@Data
@NoArgsConstructor
@Entity
@Table(name = "temporary_users")
public class TemporaryUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "main_number")
    private String mainNumber;

    @Email
    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "verified_token")
    private String verifiedToken;

    @Column(name = "added_at")
    @CreationTimestamp(source = SourceType.VM)
    private LocalDateTime addedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemporaryUser temporaryUser = (TemporaryUser) o;
        return Objects.equals(id, temporaryUser.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
