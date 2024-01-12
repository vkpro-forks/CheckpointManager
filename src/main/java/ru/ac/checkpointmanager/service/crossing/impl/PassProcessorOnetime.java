package ru.ac.checkpointmanager.service.crossing.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.PassAlreadyUsedException;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.repository.CrossingRepository;
import ru.ac.checkpointmanager.service.crossing.PassProcessor;

@Service("ONETIME")
@Slf4j
@RequiredArgsConstructor
public class PassProcessorOnetime implements PassProcessor {

    private final CrossingRepository crossingRepository;

    private static final String PASS_STATUS_CHANGED_LOG = "Pass [{}], changed status to {}";

    /**
     * Обрабатывает одноразовый пропуск типа ONETIME, ставит статус COMPLETED если пересечение закрылось
     * (после IN было OUT)
     *
     * @param pass             пропуск
     * @param currentDirection направление пересечения
     * @throws PassAlreadyUsedException если по одноразовому пропуску уже было пересечение
     */
    @Override
    public void process(Pass pass, Direction currentDirection) {
        log.debug("Processing onetime pass [{}]", pass.getId());

        if (currentDirection == Direction.OUT) {
            pass.setStatus(PassStatus.COMPLETED);
            log.info(PASS_STATUS_CHANGED_LOG, pass.getId(), pass.getStatus());
        } else if (!crossingRepository.findCrossingsByPassId(pass.getId()).isEmpty()) {
            log.warn(ExceptionUtils.PASS_ALREADY_USED.formatted(pass.getId()));
            throw new PassAlreadyUsedException(ExceptionUtils.PASS_ALREADY_USED.formatted(pass.getId()));
        } else {
            log.debug("Pass was processed for IN Direction");
        }
    }

}
