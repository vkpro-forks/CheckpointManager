package ru.ac.checkpointmanager.repository.car;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ac.checkpointmanager.model.car.Car;

import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {
}
