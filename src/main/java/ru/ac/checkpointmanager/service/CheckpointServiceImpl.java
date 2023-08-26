package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.CheckpointNotFoundException;
import ru.ac.checkpointmanager.model.Checkpoint;
import ru.ac.checkpointmanager.repository.CheckpointRepository;

import java.time.LocalDate;
import java.util.List;

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
    public Checkpoint findCheckpointById(int id) {
        return checkpointRepository.findById(id).orElseThrow(
                () -> new CheckpointNotFoundException(String.format("Room not found [userId=%d]", id)));
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
    public Checkpoint updateCheckpoint(Checkpoint checkpoint) {
        //because "addedAt" field not included in dto and after update checkpoint's data became empty
        //maybe exist better way to save this value in table?
        checkpoint.setAddedAt(checkpointRepository
                .findById(checkpoint.getId())
                .get().getAddedAt());
        return checkpointRepository.save(checkpoint);
    }

    @Override
    public void deleteCheckpointById(int id) {
        checkpointRepository.deleteById(id);
    }


}
