package ru.ac.checkpointmanager.service.crossing.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.pass.PassException;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.service.crossing.PassHandler;
import ru.ac.checkpointmanager.service.crossing.PassProcessing;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PassHandlerImpl implements PassHandler {

    private final PassRepository passRepository;

    private final Map<String, PassProcessing> passProcessingMap;

    /**
     * Обрабатывает использованный при пересечении пропуск в зависимости от его временного типа,
     * затем устанавливает ожидаемое направление следующего пересечения
     * на противоположное направлению текущего пересечения
     *
     * @param pass             пропуск, использованный при пересечении
     * @param currentDirection направление текущего (добавляемого) пересечения
     * @throws PassException если передан пропуск не поддерживаемого типа
     */
    @Override
    @Transactional
    public void handle(Pass pass, Direction currentDirection) {
        String passTimeType = pass.getTimeType().toString();
        PassProcessing passProcessing = passProcessingMap.get(passTimeType);
        if (passProcessing == null) {
            log.error(ExceptionUtils.UNSUPPORTED_PASS_TYPE.formatted(passTimeType));
            throw new PassException(ExceptionUtils.UNSUPPORTED_PASS_TYPE.formatted(passTimeType));
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
