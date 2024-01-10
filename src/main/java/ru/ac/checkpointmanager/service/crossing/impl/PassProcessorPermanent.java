package ru.ac.checkpointmanager.service.crossing.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.service.crossing.PassProcessor;

@Service("PERMANENT")
@Slf4j
public class PassProcessorPermanent implements PassProcessor {

    /**
     * Обрабатывает многоразовый пропуск типа PERMANENT, просто логирует события
     *
     * @param pass             пропуск
     * @param currentDirection направление пересечения
     */
    @Override
    public void process(Pass pass, Direction currentDirection) {
        log.debug("Processing permanent pass [{}]", pass.getId());

        if (pass.getExpectedDirection() != currentDirection) {
            log.warn("Two crossings in a row in the same direction - {}, pass {}", currentDirection, pass.getId());
        }
    }
}