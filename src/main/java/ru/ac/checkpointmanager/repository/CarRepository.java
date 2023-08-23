package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ac.checkpointmanager.model.Car;

import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {
}
