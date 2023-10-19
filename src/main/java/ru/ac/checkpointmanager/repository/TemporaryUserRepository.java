package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ac.checkpointmanager.model.TemporaryUser;

import java.util.UUID;

public interface TemporaryUserRepository extends JpaRepository<TemporaryUser, UUID> {
    TemporaryUser findByVerifiedToken(String verifiedToken);
}
