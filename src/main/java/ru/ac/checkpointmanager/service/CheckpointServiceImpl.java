package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.CheckpointNotFoundException;
import ru.ac.checkpointmanager.model.Checkpoint;
import ru.ac.checkpointmanager.repository.CheckpointRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static ru.ac.checkpointmanager.utils.StringTrimmer.trimThemAll;

@Service
@RequiredArgsConstructor
public class CheckpointServiceImpl implements CheckpointService {

    private final CheckpointRepository repository;

    @Override
    public Checkpoint addCheckpoint(Checkpoint checkpoint) {
        trimThemAll(checkpoint);
        checkpoint.setAddedAt(LocalDate.now());
        return repository.save(checkpoint);
    }

    @Override
    public Checkpoint findCheckpointById(UUID id) {
        return repository.findById(id).orElseThrow(
                () -> new CheckpointNotFoundException(String.format("Checkpoint not found [userId=%s]", id)));
    }

    @Override
    public List<Checkpoint> findCheckpointsByName(String name) {
        return repository.findCheckpointsByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Checkpoint> findAllCheckpoints() {
        return repository.findAll();
    }

    @Override
    public List<Checkpoint> findCheckpointsByTerritoryId(UUID id) {

        List<Checkpoint> foundCheckpoints = repository.findCheckpointsByTerritoryIdOrderByName(id);

        if (foundCheckpoints.isEmpty()) {
            throw new CheckpointNotFoundException(String.format("For Territory [id=%s] not exist any Checkpoints", id));
        }
        return foundCheckpoints;
    }

    @Override
    public Checkpoint updateCheckpoint(Checkpoint checkpoint) {
        trimThemAll(checkpoint);
        Checkpoint foundCheckpoint = repository.findById(checkpoint.getId())
                        .orElseThrow(() -> new CheckpointNotFoundException
                                (String.format("Checkpoint not found [Id=%s]", checkpoint.getId())));

        foundCheckpoint.setName(checkpoint.getName());
        foundCheckpoint.setType(checkpoint.getType());
        foundCheckpoint.setNote(checkpoint.getNote());
        foundCheckpoint.setTerritory(checkpoint.getTerritory());

        return repository.save(foundCheckpoint);
    }

    @Override
    public void deleteCheckpointById(UUID id) {

        if (repository.findById(id).isEmpty()) {
            throw new CheckpointNotFoundException(String.format("Checkpoint not found [Id=%s]", id));
        }
        repository.deleteById(id);
    }

}
