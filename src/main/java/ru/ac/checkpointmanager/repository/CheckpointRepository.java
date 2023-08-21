package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ac.checkpointmanager.model.Checkpoint;

@Repository
public interface CheckpointRepository extends JpaRepository<Checkpoint, Integer> {

}
