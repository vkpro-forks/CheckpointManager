package ru.ac.checkpointmanager.repository.car;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ac.checkpointmanager.model.car.Trailer;

import java.util.UUID;

public interface TrailerRepository extends JpaRepository<Trailer, UUID> {

    boolean existsByLicensePlate(String licensePlate);

}
