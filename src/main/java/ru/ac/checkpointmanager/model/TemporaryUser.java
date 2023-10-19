package ru.ac.checkpointmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * The TemporaryUser class represents a user with temporary access.
 *
 * @author fifimova
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
