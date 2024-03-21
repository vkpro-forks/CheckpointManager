package ru.ac.checkpointmanager.service.checkpoints;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.CheckpointDTO;
import ru.ac.checkpointmanager.dto.CheckpointUpdateDTO;
import ru.ac.checkpointmanager.exception.CheckpointNotFoundException;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.mapper.CheckpointMapper;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.repository.CheckpointRepository;
import ru.ac.checkpointmanager.service.territories.TerritoryService;
import ru.ac.checkpointmanager.utils.StringTrimmer;

import java.util.List;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class CheckpointServiceImpl implements CheckpointService {

    private final CheckpointRepository checkpointRepository;
    private final TerritoryService territoryService;
    private final CheckpointMapper checkpointMapper;

    @Override
    @Transactional
    public CheckpointDTO addCheckpoint(CheckpointDTO checkpointDTO) {
        Checkpoint checkpoint = checkpointMapper.toCheckpoint(checkpointDTO);

        territoryService.findById(checkpoint.getTerritory().getId());
        StringTrimmer.trimThemAll(checkpoint);
        checkpointRepository.save(checkpoint);
        return checkpointMapper.toCheckpointDTO(checkpoint);
    }

    @Override
    public CheckpointDTO findById(UUID id) {
        Checkpoint foundCheckpoint = checkpointRepository.findById(id).orElseThrow(
                () -> {
                    log.warn(ExceptionUtils.CHECKPOINT_NOT_FOUND.formatted(id));
                    return new CheckpointNotFoundException(ExceptionUtils.CHECKPOINT_NOT_FOUND.formatted(id));
                });
        return checkpointMapper.toCheckpointDTO(foundCheckpoint);
    }

    @Override
    public Checkpoint findCheckpointById(UUID id) {
        return checkpointRepository.findById(id).orElseThrow(
                () -> {
                    log.warn(ExceptionUtils.CHECKPOINT_NOT_FOUND.formatted(id));
                    return new CheckpointNotFoundException(ExceptionUtils.CHECKPOINT_NOT_FOUND.formatted(id));
                });
    }

    @Override
    public List<CheckpointDTO> findCheckpointsByName(String name) {
        List<Checkpoint> checkpoints = checkpointRepository.findCheckpointsByNameContainingIgnoreCase(name);
        return checkpointMapper.toCheckpointsDTO(checkpoints);
    }

    @Override
    public List<CheckpointDTO> findAllCheckpoints() {
        List<Checkpoint> checkpoints = checkpointRepository.findAll();
        return checkpointMapper.toCheckpointsDTO(checkpoints);
    }

    @Override
    public List<CheckpointDTO> findCheckpointsByTerritoryId(UUID id) {
        List<Checkpoint> foundCheckpoints = checkpointRepository.findCheckpointsByTerritoryIdOrderByName(id);
        log.debug("Checkpoint for [territory with id: {}] retrieved from repo", id);
        return checkpointMapper.toCheckpointsDTO(foundCheckpoints);
    }

    @Override
    @Transactional
    public CheckpointDTO updateCheckpoint(CheckpointUpdateDTO checkpointDTO) {
        UUID checkpointId = checkpointDTO.getId();
        Checkpoint foundCheckpoint = checkpointRepository.findById(checkpointId)
                .orElseThrow(() -> {
                    log.warn(ExceptionUtils.CHECKPOINT_NOT_FOUND.formatted(checkpointId));
                    return new CheckpointNotFoundException(ExceptionUtils.CHECKPOINT_NOT_FOUND.formatted(checkpointId));
                });
        StringTrimmer.trimThemAll(checkpointDTO);

        if (checkpointDTO.getName() != null) foundCheckpoint.setName(checkpointDTO.getName());
        if (checkpointDTO.getType() != null) foundCheckpoint.setType(checkpointDTO.getType());
        if (checkpointDTO.getNote() != null) foundCheckpoint.setNote(checkpointDTO.getNote());
        if (checkpointDTO.getTerritory() != null) {
            Territory territory = territoryService.findTerritoryById(checkpointDTO.getTerritory().getId());
            foundCheckpoint.setTerritory(territory);
        }

        Checkpoint updated = checkpointRepository.save(foundCheckpoint);
        log.info("[Checkpoint with id: {}] was successfully updated", checkpointId);
        return checkpointMapper.toCheckpointDTO(updated);
    }

    @Override
    public void deleteCheckpointById(UUID id) {
        if (checkpointRepository.findById(id).isEmpty()) {
            log.warn(ExceptionUtils.CHECKPOINT_NOT_FOUND.formatted(id));
            throw new CheckpointNotFoundException(ExceptionUtils.CHECKPOINT_NOT_FOUND.formatted(id));
        }
        checkpointRepository.deleteById(id);
        log.info("[Checkpoint with id: {}] successfully deleted", id);
    }
}
