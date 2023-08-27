package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ac.checkpointmanager.model.Checkpoint;

import java.util.List;

@Repository
public interface CheckpointRepository extends JpaRepository<Checkpoint, Integer> {
    List<Checkpoint> findCheckpointsByNameContainingIgnoreCase(String name);

    List<Checkpoint> findCheckpointsByTerritoryIdOrderByName(Integer id);
}
