package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.CheckpointNotFoundException;
import ru.ac.checkpointmanager.model.Checkpoint;
import ru.ac.checkpointmanager.repository.CheckpointRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckpointServiceImpl implements CheckpointService {

    private final CheckpointRepository checkpointRepository;

    @Override
    public Checkpoint addCheckpoint(Checkpoint checkpoint) {

        checkpoint.setAddedAt(LocalDate.now());
        return checkpointRepository.save(checkpoint);
    }

    @Override
    public Checkpoint findCheckpointById(UUID id) {
        return checkpointRepository.findById(id).orElseThrow(
                () -> new CheckpointNotFoundException(String.format("Room not found [userId=%s]", id)));
    }

    @Override
    public List<Checkpoint> findCheckpointsByName(String name) {
        return checkpointRepository.findCheckpointsByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Checkpoint> findAllCheckpoints() {
        return checkpointRepository.findAll();
    }

    @Override
    public List<Checkpoint> findCheckpointsByTerritoryId(UUID id) {

        List<Checkpoint> foundCheckpoints = checkpointRepository.findCheckpointsByTerritoryIdOrderByName(id);

        if (foundCheckpoints.isEmpty()) {
            throw new CheckpointNotFoundException(String.format("For Territory [id=%s] not exist any Checkpoints", id));
        }
        return foundCheckpoints;
    }

    @Override
    public Checkpoint updateCheckpoint(Checkpoint checkpoint) {

        Checkpoint foundCheckpoint = checkpointRepository.findById(checkpoint.getId())
                        .orElseThrow(() -> new CheckpointNotFoundException
                                (String.format("Checkpoint not found [Id=%s]", checkpoint.getId())));

        foundCheckpoint.setName(checkpoint.getName());
        foundCheckpoint.setType(checkpoint.getType());
        foundCheckpoint.setNote(checkpoint.getNote());
        foundCheckpoint.setTerritory(checkpoint.getTerritory());

        return checkpointRepository.save(foundCheckpoint);
    }

    @Override
    public void deleteCheckpointById(UUID id) {

        if (checkpointRepository.findById(id).isEmpty()) {
            throw new CheckpointNotFoundException(String.format("Checkpoint not found [Id=%s]", id));
        }
        checkpointRepository.deleteById(id);
    }

}
