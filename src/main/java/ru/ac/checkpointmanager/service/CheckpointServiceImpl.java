package ru.ac.checkpointmanager.service;

import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.model.Checkpoint;
import ru.ac.checkpointmanager.repository.CheckpointRepository;

@Service
public class CheckpointServiceImpl implements CheckpointService {

    private final CheckpointRepository checkpointRepository;

    public CheckpointServiceImpl(CheckpointRepository checkpointRepository) {
        this.checkpointRepository = checkpointRepository;
    }

    @Override
    public void addCheckpoint(Checkpoint checkpoint) {
        checkpointRepository.save(checkpoint);
    }
}
