package ru.ac.checkpointmanager.service.crossing.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.PassProcessorException;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.service.crossing.CrossingPassHandler;
import ru.ac.checkpointmanager.service.crossing.PassProcessor;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
class CrossingPassHandlerImpl implements CrossingPassHandler {

    private final PassRepository passRepository;

    private final Map<String, PassProcessor> passProcessingMap;

    /**
     * Обрабатывает использованный при пересечении пропуск в зависимости от его временного типа,
     * затем устанавливает ожидаемое направление следующего пересечения
     * на противоположное направлению текущего пересечения
     *
     * @param pass             пропуск, использованный при пересечении
     * @param currentDirection направление текущего (добавляемого) пересечения
     * @throws PassProcessorException процессор для типа пропуска не установлен
     */
    @Override
    @Transactional
    public void handle(Pass pass, Direction currentDirection) {
        String passTimeType = pass.getTimeType().toString();
        PassProcessor passProcessor = passProcessingMap.get(passTimeType);
        if (passProcessor == null) {
            log.error(ExceptionUtils.UNSUPPORTED_PASS_TYPE.formatted(passTimeType));
            throw new PassProcessorException(ExceptionUtils.UNSUPPORTED_PASS_TYPE.formatted(passTimeType));
        }

        passProcessor.process(pass, currentDirection);

        Direction nextDirectionForUsedPass = switch (currentDirection) {
            case IN -> Direction.OUT;
            case OUT -> Direction.IN;
        };
        pass.setExpectedDirection(nextDirectionForUsedPass);
        log.debug("Pass [{}], changed expected direction to {}", pass.getId(), nextDirectionForUsedPass);

        passRepository.save(pass);
    }

}
