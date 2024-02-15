package ru.ac.checkpointmanager.security.authfacade;

import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.repository.TerritoryRepository;

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
}
