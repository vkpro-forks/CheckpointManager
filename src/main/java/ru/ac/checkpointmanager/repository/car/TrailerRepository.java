package ru.ac.checkpointmanager.repository.car;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ac.checkpointmanager.model.car.Trailer;

public interface TrailerRepository extends JpaRepository<Trailer, Long> {
}
