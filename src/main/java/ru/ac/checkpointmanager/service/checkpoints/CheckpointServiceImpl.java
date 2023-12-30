package ru.ac.checkpointmanager.service.checkpoints;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.CheckpointDTO;
import ru.ac.checkpointmanager.exception.CheckpointNotFoundException;
import ru.ac.checkpointmanager.mapper.CheckpointMapper;
import ru.ac.checkpointmanager.model.Territory;
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
    private final CheckpointMapper checkpointMapper;

    @Override
    @Transactional
    public CheckpointDTO addCheckpoint(CheckpointDTO checkpointDTO) {
        log.info(METHOD_CALLED_LOG, MethodLog.getMethodName(), checkpointDTO.getId());
        Checkpoint checkpoint = checkpointMapper.toCheckpoint(checkpointDTO);

        territoryService.findById(checkpoint.getTerritory().getId());
        trimThemAll(checkpoint);
        checkpointRepository.save(checkpoint);
        return checkpointMapper.toCheckpointDTO(checkpoint);
    }

    @Override
    public CheckpointDTO findById(UUID id) {
        log.debug(METHOD_CALLED_LOG, MethodLog.getMethodName(), id);
        Checkpoint foundCheckpoint = checkpointRepository.findById(id).orElseThrow(
                () -> {
                    log.warn(CHECKPOINT_NOT_FOUND_LOG, id);
                    return new CheckpointNotFoundException(CHECKPOINT_NOT_FOUND_MSG.formatted(id));
                });
        return checkpointMapper.toCheckpointDTO(foundCheckpoint);
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
    public List<CheckpointDTO> findCheckpointsByName(String name) {
        log.debug("Method {}, name - {}", MethodLog.getMethodName(), name);
        List<Checkpoint> checkpoints = checkpointRepository.findCheckpointsByNameContainingIgnoreCase(name);
        return checkpointMapper.toCheckpointsDTO(checkpoints);
    }

    @Override
    public List<CheckpointDTO> findAllCheckpoints() {
        log.debug("Method {}", MethodLog.getMethodName());
        List<Checkpoint> checkpoints = checkpointRepository.findAll();
        return checkpointMapper.toCheckpointsDTO(checkpoints);
    }

    @Override
    public List<CheckpointDTO> findCheckpointsByTerritoryId(UUID id) {
        log.debug(METHOD_CALLED_LOG, MethodLog.getMethodName(), id);
        List<Checkpoint> foundCheckpoints = checkpointRepository.findCheckpointsByTerritoryIdOrderByName(id);
        log.debug("Checkpoint for [territory with id: {}] retrieved from repo", id);
        return checkpointMapper.toCheckpointsDTO(foundCheckpoints);
    }

    @Override
    @Transactional
    public CheckpointDTO updateCheckpoint(CheckpointDTO checkpointDTO) {
        UUID checkpointId = checkpointDTO.getId();
        log.info(METHOD_CALLED_LOG, MethodLog.getMethodName(), checkpointId);
        Checkpoint foundCheckpoint = checkpointRepository.findById(checkpointId)
                .orElseThrow(() -> {
                    log.warn(CHECKPOINT_NOT_FOUND_LOG, checkpointId);
                    return new CheckpointNotFoundException(CHECKPOINT_NOT_FOUND_MSG.formatted(checkpointId));
                });
        //FIXME It would be better to find entity here and bind it to checkpoint
        //FIXME we don't need to go to DB if territory shouldn't change
        Territory territory = territoryService.findTerritoryById(checkpointDTO.getTerritory().getId());
        trimThemAll(checkpointDTO);

        foundCheckpoint.setName(checkpointDTO.getName());
        foundCheckpoint.setType(checkpointDTO.getType());
        foundCheckpoint.setNote(checkpointDTO.getNote());
        foundCheckpoint.setTerritory(territory);

        Checkpoint updated = checkpointRepository.save(foundCheckpoint);
        log.info("[Checkpoint with id: {}] was successfully updated", checkpointId);
        return checkpointMapper.toCheckpointDTO(updated);
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
