package ru.ac.checkpointmanager.service.passes.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.MismatchedTerritoryException;
import ru.ac.checkpointmanager.exception.pass.InactivePassException;
import ru.ac.checkpointmanager.exception.pass.UserTerritoryRelationException;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.checkpoints.CheckpointType;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.service.passes.PassChecker;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PassCheckerImpl implements PassChecker {

    private final PassRepository passRepository;

    /**
     * Проверяет связь пользователя и территории,
     * которая означает право пользователя создавать пропуска на указанную территорию
     *
     * @param userId      id пользователя
     * @param territoryId id территории
     * @throws UserTerritoryRelationException если у пользователя нет права на создание пропуск
     */
    @Override
    public void checkUserTerritoryRelation(UUID userId, UUID territoryId) {
        if (!passRepository.checkUserTerritoryRelation(userId, territoryId)) {
            log.warn(ExceptionUtils.USER_TER_REL_MSG.formatted(userId, territoryId));
            throw new UserTerritoryRelationException(ExceptionUtils.USER_TER_REL_MSG.formatted(userId, territoryId));
        }
    }

    /**
     * Проверяет активен ли пропуск
     *
     * @param pass пропуск
     * @throws InactivePassException        если пропуск не активен
     * @throws MismatchedTerritoryException если территория пропуск не соответствует территории чекпоинта
     */
    @Override
    public void checkPassActivity(Pass pass) {
        if (pass.getStatus() != PassStatus.ACTIVE) {
            log.warn(ExceptionUtils.INACTIVE_PASS.formatted(pass.getId()));
            throw new InactivePassException(ExceptionUtils.INACTIVE_PASS.formatted(pass.getId()));
        }
    }

    /**
     * Проверяет, принадлежит ли пропуск и чекпоинт одной территории
     *
     * @param pass       пропуск
     * @param checkpoint чекпоинт
     * @throws MismatchedTerritoryException если территория пропуск не соответствует территории чекпоинта
     */
    @Override
    public void checkPassAndCheckpointTerritories(Pass pass, Checkpoint checkpoint) {
        Territory checkPointTerritory = checkpoint.getTerritory();
        Territory passTerritory = pass.getTerritory();
        if (checkPointTerritory == null ||
                passTerritory == null ||
                !checkPointTerritory.getId().equals(passTerritory.getId())) {
            log.warn(ExceptionUtils.PASS_MISMATCHED_TERRITORY.formatted(pass.getId(),
                    passTerritory != null ? passTerritory.getId() : "null",
                    checkPointTerritory != null ? checkPointTerritory.getId() : "null"
            ));
            throw new MismatchedTerritoryException(ExceptionUtils.PASS_MISMATCHED_TERRITORY.formatted(pass.getId(),
                    passTerritory != null ? passTerritory.getId() : "null",
                    checkPointTerritory != null ? checkPointTerritory.getId() : "null"));
        }
    }

    @Override
    public void checkPassAndCheckpointCompatibility(Pass pass, Checkpoint checkpoint) {
        if (checkpoint.getType() != CheckpointType.UNIVERSAL &&
                !pass.getDtype().equals(checkpoint.getType().toString())) {
            log.warn("Conflict between the types of pass and checkpoint [pass - %s, %s], [checkpoint - %s, %s]"
                    .formatted(pass.getId(), pass.getDtype(), checkpoint.getId(), checkpoint.getType()));
        }
    }
}
