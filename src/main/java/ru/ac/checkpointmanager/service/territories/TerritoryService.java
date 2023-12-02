package ru.ac.checkpointmanager.service.territories;

import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.model.Territory;

import java.util.List;
import java.util.UUID;

public interface TerritoryService {

    TerritoryDTO addTerritory(TerritoryDTO territoryDTO);

    TerritoryDTO findById(UUID id);

    Territory findTerritoryById(UUID id);

    List<UserResponseDTO> findUsersByTerritoryId(UUID territoryId);

    List<TerritoryDTO> findTerritoriesByName(String name);

    List<TerritoryDTO> findAllTerritories();

    TerritoryDTO updateTerritory(TerritoryDTO territoryDTO);

    void attachUserToTerritory(UUID territoryId, UUID userId);

    void deleteTerritoryById(UUID id);

    void detachUserFromTerritory(UUID territoryId, UUID userId);

    Territory findByPassId(UUID passId);
}
