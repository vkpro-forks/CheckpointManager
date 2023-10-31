package ru.ac.checkpointmanager.service.checkpoints;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.CheckpointNotFoundException;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.repository.CheckpointRepository;
import ru.ac.checkpointmanager.service.territories.TerritoryService;
import ru.ac.checkpointmanager.service.avatar.AvatarService;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.util.List;
import java.util.UUID;

import static ru.ac.checkpointmanager.utils.StringTrimmer.trimThemAll;

@Service
@Slf4j
@RequiredArgsConstructor
public class CheckpointServiceImpl implements CheckpointService {

    private final CheckpointRepository repository;
    private final TerritoryService territoryService;
    private final AvatarService avatarService;

    @Override
    public Checkpoint addCheckpoint(Checkpoint checkpoint) {
        log.info("Method {}, UUID - {}", MethodLog.getMethodName(), checkpoint.getId());
        territoryService.findTerritoryById(checkpoint.getTerritory().getId());
        trimThemAll(checkpoint);
        return repository.save(checkpoint);
    }

    @Override
    public Checkpoint findCheckpointById(UUID id) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), id);

        return repository.findById(id).orElseThrow(
                () -> new CheckpointNotFoundException(String.format("Checkpoint not found [userId=%s]", id)));
    }

    @Override
    public List<Checkpoint> findCheckpointsByName(String name) {
        log.debug("Method {}, name - {}", MethodLog.getMethodName(), name);
        return repository.findCheckpointsByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Checkpoint> findAllCheckpoints() {
        log.debug("Method {}", MethodLog.getMethodName());
        return repository.findAll();
    }

    @Override
    public List<Checkpoint> findCheckpointsByTerritoryId(UUID id) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), id);

        List<Checkpoint> foundCheckpoints = repository.findCheckpointsByTerritoryIdOrderByName(id);

        if (foundCheckpoints.isEmpty()) {
            throw new CheckpointNotFoundException(String.format("For Territory [id=%s] not exist any Checkpoints", id));
        }
        return foundCheckpoints;
    }

    @Override
    public Checkpoint updateCheckpoint(Checkpoint checkpoint) {
        log.info("Method {}, UUID - {}", MethodLog.getMethodName(), checkpoint.getId());
        Checkpoint foundCheckpoint = repository.findById(checkpoint.getId())
                        .orElseThrow(() -> new CheckpointNotFoundException
                                (String.format("Checkpoint not found [Id=%s]", checkpoint.getId())));

        territoryService.findTerritoryById(checkpoint.getTerritory().getId());
        trimThemAll(checkpoint);

        foundCheckpoint.setName(checkpoint.getName());
        foundCheckpoint.setType(checkpoint.getType());
        foundCheckpoint.setNote(checkpoint.getNote());
        foundCheckpoint.setTerritory(checkpoint.getTerritory());

        return repository.save(foundCheckpoint);
    }

    @Override
    public void deleteCheckpointById(UUID id) {
        log.info("Method {}, UUID - {}", MethodLog.getMethodName(), id);

        if (repository.findById(id).isEmpty()) {
            throw new CheckpointNotFoundException(String.format("Checkpoint not found [Id=%s]", id));
        }
        repository.deleteById(id);
        avatarService.deleteAvatarIfExists(id);
    }
}
