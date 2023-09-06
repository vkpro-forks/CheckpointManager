package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ac.checkpointmanager.model.Phone;

import java.util.UUID;

public interface PhoneRepository extends JpaRepository<Phone, UUID> {
}
