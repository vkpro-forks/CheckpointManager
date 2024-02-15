package ru.ac.checkpointmanager.security.authfacade;

import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.service.checkpoints.CheckpointService;

import java.util.UUID;

@Component("checkpointAuthFacade")
public final class CheckpointAuthFacade implements AuthFacade {

    private final CheckpointService checkpointService;
    private final TerritoryRepository territoryRepository;

    private CheckpointAuthFacade(CheckpointService checkpointService, TerritoryRepository territoryRepository) {
        this.checkpointService = checkpointService;
        this.territoryRepository = territoryRepository;
    }

    @Override
    public boolean isIdMatch(UUID checkpointId) {
        UUID userId = getCurrentUser().getId();
        Checkpoint checkpoint = checkpointService.findCheckpointById(checkpointId);
        UUID territoryId = checkpoint.getTerritory().getId();
        return territoryRepository.checkUserTerritoryRelation(userId, territoryId);
    }
}
