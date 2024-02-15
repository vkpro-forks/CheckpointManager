package ru.ac.checkpointmanager.security.authfacade;

import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.service.passes.PassService;

import java.util.UUID;

@Component("passAuthFacade")
public final class PassAuthFacade implements AuthFacade {

    private final PassService passService;
    private final TerritoryRepository territoryRepository;

    private PassAuthFacade(PassService passService, TerritoryRepository territoryRepository) {
        this.passService = passService;
        this.territoryRepository = territoryRepository;
    }

    @Override
    public boolean isIdMatch(UUID passId) {
        UUID userId = getCurrentUser().getId();
        Pass pass = passService.findPassById(passId);
        return userId.equals(pass.getUser().getId());
    }

    public boolean isTerritoryIdMatch(UUID passId) {
        UUID userId = getCurrentUser().getId();
        Pass pass = passService.findPassById(passId);
        UUID territoryId = pass.getTerritory().getId();
        return territoryRepository.checkUserTerritoryRelation(userId, territoryId);
    }
}
