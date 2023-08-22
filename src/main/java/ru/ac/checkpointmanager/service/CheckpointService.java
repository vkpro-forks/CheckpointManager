package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.model.Checkpoint;

import java.util.List;

public interface CheckpointService {

    Checkpoint addCheckpoint(Checkpoint checkpoint);

    Checkpoint findCheckpointById(int id);

    List<Checkpoint> findCheckpointsByName(String name);

    List<Checkpoint> findAllCheckpoints();

    Checkpoint updateCheckpoint(Checkpoint checkpoint);

    void deleteCheckpointById(int id);
}
