package ru.ac.checkpointmanager.service.crossing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.exception.CrossingNotFoundException;
import ru.ac.checkpointmanager.exception.pass.InactivePassException;
import ru.ac.checkpointmanager.exception.MismatchedTerritoryException;
import ru.ac.checkpointmanager.mapper.CrossingMapper;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.checkpoints.CheckpointType;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.repository.CrossingRepository;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.service.checkpoints.CheckpointService;
import ru.ac.checkpointmanager.service.passes.PassService;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.util.Map;
import java.util.UUID;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class CrossingServiceImpl implements CrossingService {

    private static final String METHOD_UUID = "Method {} [{}]";

    private final CrossingRepository crossingRepository;
    private final PassService passService;
    private final PassRepository passRepository;
    private final CheckpointService checkpointService;
    private final CrossingMapper mapper;
    private final Map<String, PassProcessing> passProcessingMap;

    @Override
    public CrossingDTO addCrossing(CrossingDTO crossingDTO) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), crossingDTO);
        UUID passId = crossingDTO.getPassId();
        Pass pass = passService.findPassById(passId);
        if (pass.getStatus() != PassStatus.ACTIVE) {
            log.warn("The pass is not active now %s".formatted(passId));
            throw new InactivePassException("The pass is not active now %s".formatted(passId));
        }

        UUID checkpointId = crossingDTO.getCheckpointId();
        Checkpoint checkpoint = checkpointService.findCheckpointById(checkpointId);
        if (checkpoint.getType() != CheckpointType.UNIVERSAL &&
            !pass.getDtype().equals(checkpoint.getType().toString())) {
            log.warn("Conflict between the types of pass and checkpoint [pass - %s, %s], [checkpoint - %s, %s]"
                .formatted(pass.getId(), pass.getDtype(), checkpoint.getId(), checkpoint.getType()));
        }
        if (!checkpoint.getTerritory().equals(pass.getTerritory())) {
            log.warn("Pass [%s] is issued to another territory [%s]".formatted(passId, pass.getTerritory()));
            throw new MismatchedTerritoryException("Pass [%s] is issued to another territory [%s]"
                .formatted(passId, pass.getTerritory()));
        }

        processPass(pass, crossingDTO.getDirection());

        Crossing crossing = mapper.toCrossing(crossingDTO, pass, checkpoint);
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

    /**
     * Обрабатывает использованный при пересечении пропуск в зависимости от его временного типа,
     * затем устанавливает ожидаемое направление следующего пересечения
     * на противоположное направлению текущего пересечения
     *
     * @param pass пропуск, использованные при пересечении
     * @param currentDirection направление текущего (добавляемого) пересечения
     */
    public void processPass(Pass pass, Direction currentDirection) {
        String passTimeType = pass.getTypeTime().toString();
        PassProcessing passProcessing = passProcessingMap.get(passTimeType);
        if (passProcessing == null) {
            log.error("Unsupported pass time type - %s".formatted(passTimeType));
            throw new RuntimeException("Unsupported pass time type - %s".formatted(passTimeType));
        }
        passProcessing.process(pass, currentDirection);

        Direction nextDirectionForUsedPass = switch (currentDirection) {
            case IN -> Direction.OUT;
            case OUT -> Direction.IN;
        };
        pass.setExpectedDirection(nextDirectionForUsedPass);
        log.debug("Pass [{}], changed expected direction to {}", pass.getId(), nextDirectionForUsedPass);

        passRepository.save(pass);
    }
}

