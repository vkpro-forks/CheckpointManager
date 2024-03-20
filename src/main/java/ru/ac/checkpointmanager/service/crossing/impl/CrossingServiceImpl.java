package ru.ac.checkpointmanager.service.crossing.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.dto.CrossingRequestDTO;
import ru.ac.checkpointmanager.dto.passes.PagingParams;
import ru.ac.checkpointmanager.exception.CrossingNotFoundException;
import ru.ac.checkpointmanager.exception.MismatchedTerritoryException;
import ru.ac.checkpointmanager.exception.pass.InactivePassException;
import ru.ac.checkpointmanager.mapper.CrossingMapper;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.repository.CrossingRepository;
import ru.ac.checkpointmanager.service.checkpoints.CheckpointService;
import ru.ac.checkpointmanager.service.crossing.CrossingPassHandler;
import ru.ac.checkpointmanager.service.crossing.CrossingService;
import ru.ac.checkpointmanager.service.passes.PassChecker;
import ru.ac.checkpointmanager.service.passes.PassService;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.time.ZonedDateTime;
import java.util.List;
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
    private final PassChecker passChecker;
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
    @Override
    @Transactional
    public CrossingDTO addCrossing(CrossingRequestDTO crossingDTO, Direction direction) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), crossingDTO);

        UUID passId = crossingDTO.getPassId();
        Pass pass = passService.findPassById(passId);
        passChecker.checkPassActivity(pass);

        UUID checkpointId = crossingDTO.getCheckpointId();
        Checkpoint checkpoint = checkpointService.findCheckpointById(checkpointId);
        passChecker.checkPassAndCheckpointTerritories(pass, checkpoint);
        passChecker.checkPassAndCheckpointCompatibility(pass, checkpoint);

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

    @Override
    public List<CrossingDTO> getByPassId(UUID passId, PagingParams pagingParams) {
        passService.findPassById(passId);
        return mapper.toCrossingsDTO(crossingRepository.findCrossingsByPassId(passId,
                PageRequest.of(pagingParams.getPage(), pagingParams.getSize())));
    }

    private Crossing toCrossing(Direction direction, Pass pass, Checkpoint checkpoint, ZonedDateTime performedAt) {
        Crossing crossing = new Crossing();
        crossing.setPass(pass);
        crossing.setCheckpoint(checkpoint);
        crossing.setDirection(direction);
        crossing.setPerformedAt(performedAt);
        return crossing;
    }

}

