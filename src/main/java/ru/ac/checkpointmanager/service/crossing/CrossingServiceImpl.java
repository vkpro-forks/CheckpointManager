package ru.ac.checkpointmanager.service.crossing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.dto.CrossingRequestDTO;
import ru.ac.checkpointmanager.exception.CrossingNotFoundException;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.MismatchedTerritoryException;
import ru.ac.checkpointmanager.exception.pass.InactivePassException;
import ru.ac.checkpointmanager.mapper.CrossingMapper;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.checkpoints.CheckpointType;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.repository.CrossingRepository;
import ru.ac.checkpointmanager.service.checkpoints.CheckpointService;
import ru.ac.checkpointmanager.service.passes.PassService;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.time.ZonedDateTime;
import java.util.UUID;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class CrossingServiceImpl implements CrossingService {

    private static final String METHOD_UUID = "Method {} [{}]";

    private final CrossingRepository crossingRepository;
    private final PassService passService;
    private final CheckpointService checkpointService;
    private final CrossingPassHandler crossingPassHandler;
    private final CrossingMapper mapper;

    /**
     * Добавляет пересечение
     *
     * @param crossingDTO параметры пересечения
     * @param direction   направление
     * @return {@link CrossingDTO} сохраненное пересечение со всеми необходимыми для отображения параметрами
     * @throws InactivePassException        если пропуск не активен
     * @throws MismatchedTerritoryException если территория пропуск не соответствует территории чекпоинта
     */
    @Transactional
    @Override
    public CrossingDTO addCrossing(CrossingRequestDTO crossingDTO, Direction direction) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), crossingDTO);

        UUID passId = crossingDTO.getPassId();
        Pass pass = passService.findPassById(passId);
        checkPassIsActive(pass);

        UUID checkpointId = crossingDTO.getCheckpointId();
        Checkpoint checkpoint = checkpointService.findCheckpointById(checkpointId);
        checkPassAndCheckpointTerritories(checkpoint, pass);
        checkPassAndCheckpointAreCompatible(checkpoint, pass);

        crossingPassHandler.handle(pass, direction);
        Crossing crossing = toCrossing(direction, pass, checkpoint, crossingDTO.getPerformedAt());
        crossing = crossingRepository.save(crossing);
        log.info("Crossing added [{}]", crossing);
        return mapper.toCrossingDTO(crossing);
    }

    @Override
    public CrossingDTO getCrossing(UUID crossingId) {
        Crossing crossing = crossingRepository.findById(crossingId).orElseThrow(() -> {
            log.warn("[Crossing with id: {}] not found", crossingId);
            return new CrossingNotFoundException("Crossing with id %s not found".formatted(crossingId));
        });
        log.debug("Retrieved crossing with id {}", crossingId);
        return mapper.toCrossingDTO(crossing);
    }

    private Crossing toCrossing(Direction direction, Pass pass, Checkpoint checkpoint, ZonedDateTime performedAt) {
        Crossing crossing = new Crossing();
        crossing.setPass(pass);
        crossing.setCheckpoint(checkpoint);
        crossing.setDirection(direction);
        crossing.setPerformedAt(performedAt);
        return crossing;
    }

    private void checkPassIsActive(Pass pass) {
        if (pass.getStatus() != PassStatus.ACTIVE) {
            log.warn(ExceptionUtils.INACTIVE_PASS.formatted(pass.getId()));
            throw new InactivePassException(ExceptionUtils.INACTIVE_PASS.formatted(pass.getId()));
        }
    }

    private void checkPassAndCheckpointTerritories(Checkpoint checkpoint, Pass pass) {
        if (!checkpoint.getTerritory().equals(pass.getTerritory())) {
            log.warn(ExceptionUtils.PASS_MISMATCHED_TERRITORY.formatted(pass.getId(), pass.getTerritory()));
            throw new MismatchedTerritoryException(ExceptionUtils.PASS_MISMATCHED_TERRITORY
                    .formatted(pass.getId(), pass.getTerritory()));
        }
    }

    private void checkPassAndCheckpointAreCompatible(Checkpoint checkpoint, Pass pass) {
        if (checkpoint.getType() != CheckpointType.UNIVERSAL &&
                !pass.getDtype().equals(checkpoint.getType().toString())) {
            log.warn("Conflict between the types of pass and checkpoint [pass - %s, %s], [checkpoint - %s, %s]"
                    .formatted(pass.getId(), pass.getDtype(), checkpoint.getId(), checkpoint.getType()));
        }
    }

}

