package ru.ac.checkpointmanager.security.authfacade;

import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.model.enums.Role;

import java.util.UUID;

@Component("territoryAuthFacade")
public final class TerritoryAuthFacade implements AuthFacade {

    private final TerritoryRepository territoryRepository;

    private TerritoryAuthFacade(TerritoryRepository territoryRepository) {
        this.territoryRepository = territoryRepository;
    }

    @Override
    public boolean isIdMatch(UUID territoryId) {
        UUID userId = getCurrentUser().getId();
        return territoryRepository.checkUserTerritoryRelation(userId, territoryId);
    }

    /**
     * Проверка, относится ли пользователь ({@link Role#USER})
     * с переданным Id и текущий пользователь ({@link Role#MANAGER}, {@link Role#SECURITY}) к одной территории
     *
     * @param userId UUID пользователя, которого необходимо проверить на совпадение.
     * @return true - если пользователи относятся к одной территории,
     * false - если нет
     */
    public boolean isUserIdMatch(UUID userId) {
        UUID currentUserId = getCurrentUser().getId();
        return territoryRepository.checkIfUsersAreInTheSameTerritory(currentUserId, userId);
    }
}
