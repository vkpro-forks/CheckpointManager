package ru.ac.checkpointmanager.service.checkpoints;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.CheckpointNotFoundException;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.repository.CheckpointRepository;
import ru.ac.checkpointmanager.service.territories.TerritoryService;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.util.List;
import java.util.UUID;

import static ru.ac.checkpointmanager.utils.StringTrimmer.trimThemAll;

@Service
@Slf4j
@RequiredArgsConstructor
public class CheckpointServiceImpl implements CheckpointService {

    private static final String CHECKPOINT_NOT_FOUND_LOG = "[Checkpoint with id: {}] not found";
    private static final String CHECKPOINT_NOT_FOUND_MSG = "Checkpoint with id: %s not found";
    private static final String METHOD_CALLED_LOG = "Method {}, UUID - {}";

    private final CheckpointRepository checkpointRepository;

    private final TerritoryService territoryService;

    @Override
    public Checkpoint addCheckpoint(Checkpoint checkpoint) {
        log.info(METHOD_CALLED_LOG, MethodLog.getMethodName(), checkpoint.getId());
        territoryService.findTerritoryById(checkpoint.getTerritory().getId());
        trimThemAll(checkpoint);
        return checkpointRepository.save(checkpoint);
    }

    @Override
    public Checkpoint findCheckpointById(UUID id) {
        log.debug(METHOD_CALLED_LOG, MethodLog.getMethodName(), id);
        return checkpointRepository.findById(id).orElseThrow(
                () -> {
                    log.warn(CHECKPOINT_NOT_FOUND_LOG, id);
                    return new CheckpointNotFoundException(CHECKPOINT_NOT_FOUND_MSG.formatted(id));
                });
    }

    @Override
    public List<Checkpoint> findCheckpointsByName(String name) {
        log.debug("Method {}, name - {}", MethodLog.getMethodName(), name);
        return checkpointRepository.findCheckpointsByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Checkpoint> findAllCheckpoints() {
        log.debug("Method {}", MethodLog.getMethodName());
        return checkpointRepository.findAll();
    }

    @Override
    public List<Checkpoint> findCheckpointsByTerritoryId(UUID id) {
        log.debug(METHOD_CALLED_LOG, MethodLog.getMethodName(), id);
        List<Checkpoint> foundCheckpoints = checkpointRepository.findCheckpointsByTerritoryIdOrderByName(id);
        log.debug("Checkpoint for [territory with id: {}] retrieved from repo", id);
        return foundCheckpoints;
    }

    @Override
    public Checkpoint updateCheckpoint(Checkpoint checkpoint) {
        UUID checkpointId = checkpoint.getId();
        log.info(METHOD_CALLED_LOG, MethodLog.getMethodName(), checkpointId);
        Checkpoint foundCheckpoint = checkpointRepository.findById(checkpointId)
                .orElseThrow(() -> {
                    log.warn(CHECKPOINT_NOT_FOUND_LOG, checkpointId);
                    return new CheckpointNotFoundException(CHECKPOINT_NOT_FOUND_MSG.formatted(checkpointId));
                });
        territoryService.findTerritoryById(checkpoint.getTerritory().getId());
        trimThemAll(checkpoint);

        foundCheckpoint.setName(checkpoint.getName());
        foundCheckpoint.setType(checkpoint.getType());
        foundCheckpoint.setNote(checkpoint.getNote());
        foundCheckpoint.setTerritory(checkpoint.getTerritory());

        Checkpoint updated = checkpointRepository.save(foundCheckpoint);
        log.info("[Checkpoint with id: {}] was successfully updated", checkpointId);
        return updated;
    }

    @Override
    public void deleteCheckpointById(UUID id) {
        log.info(METHOD_CALLED_LOG, MethodLog.getMethodName(), id);
        if (checkpointRepository.findById(id).isEmpty()) {
            log.warn(CHECKPOINT_NOT_FOUND_LOG, id);
            throw new CheckpointNotFoundException(CHECKPOINT_NOT_FOUND_MSG.formatted(id));
        }
        checkpointRepository.deleteById(id);
        log.info("[Checkpoint with id: {}] successfully deleted", id);
    }
}
