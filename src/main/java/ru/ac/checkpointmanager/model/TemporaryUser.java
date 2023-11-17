package ru.ac.checkpointmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Класс TemporaryUser представляет временный объект пользователя, содержит всю необходимую информацию
 * для создания основного пользователя приложения.
 *
 * @author fifimova
 * @see User
 */

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "temporary_users")
public class TemporaryUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private UUID id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "main_number")
    private String mainNumber;

    @Email
    @Column(name = "email")
    private String email;

    @Column(name = "previous_email")
    private String previousEmail;

    @Column(name = "password")
    private String password;

    @Column(name = "verified_token")
    private String verifiedToken;

    @Column(name = "added_at")
    @CreationTimestamp(source = SourceType.VM)
    private LocalDateTime addedAt;
}