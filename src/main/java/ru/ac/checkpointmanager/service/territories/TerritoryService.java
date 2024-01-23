package ru.ac.checkpointmanager.service.territories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.model.Territory;

import java.util.List;
import java.util.UUID;

public interface TerritoryService {

    TerritoryDTO addTerritory(TerritoryDTO territoryDTO);

    TerritoryDTO findById(UUID territoryId);

    Territory findTerritoryById(UUID territoryId);

    Page<UserResponseDTO> findUsersByTerritoryId(UUID territoryId, Pageable pageable);

    List<TerritoryDTO> findTerritoriesByName(String name);

    List<TerritoryDTO> findAllTerritories();

    TerritoryDTO updateTerritory(TerritoryDTO territoryDTO);

    void attachUserToTerritory(UUID territoryId, UUID userId);

    void deleteTerritoryById(UUID territoryId);

    void detachUserFromTerritory(UUID territoryId, UUID userId);

    Territory findByPassId(UUID passId);

}
