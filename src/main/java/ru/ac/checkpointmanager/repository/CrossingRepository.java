package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.Pass;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CrossingRepository extends JpaRepository<Crossing, UUID> {

    Optional<Crossing> findTopByPassOrderByIdDesc(Pass pass);
}
