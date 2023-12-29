package ru.ac.checkpointmanager.service.passes.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.pass.UserTerritoryRelationException;
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
            log.warn(ExceptionUtils.USER_TER_REL.formatted(userId, territoryId));
            throw new UserTerritoryRelationException(ExceptionUtils.USER_TER_REL.formatted(userId, territoryId));
        }
    }

}
