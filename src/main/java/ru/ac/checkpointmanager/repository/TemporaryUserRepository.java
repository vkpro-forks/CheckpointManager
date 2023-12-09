package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ac.checkpointmanager.model.TemporaryUser;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TemporaryUserRepository extends JpaRepository<TemporaryUser, UUID> {
    Optional<TemporaryUser> findByVerifiedToken(String verifiedToken);

    void deleteByAddedAtBefore(LocalDateTime timestamp);
}
