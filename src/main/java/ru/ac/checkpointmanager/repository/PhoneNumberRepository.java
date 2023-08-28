package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ac.checkpointmanager.model.PhoneNumber;

import java.util.UUID;

public interface PhoneNumberRepository extends JpaRepository<PhoneNumber, UUID> {
}
