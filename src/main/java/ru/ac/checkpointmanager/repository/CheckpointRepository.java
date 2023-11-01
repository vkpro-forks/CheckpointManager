package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;

import java.util.List;
import java.util.UUID;

@Repository
public interface CheckpointRepository extends JpaRepository<Checkpoint, UUID> {
    List<Checkpoint> findCheckpointsByNameContainingIgnoreCase(String name);

    List<Checkpoint> findCheckpointsByTerritoryIdOrderByName(UUID id);
}
