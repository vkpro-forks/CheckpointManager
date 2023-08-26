package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.model.Territory;

import java.util.List;

public interface TerritoryService {

    Territory addTerritory(Territory territory);

    Territory findTerritoryById(int id);

    List<Territory> findTerritoriesByName(String name);

    List<Territory> findAllTerritories();

    Territory updateTerritory(Territory territory);

    void deleteTerritoryById(int id);
}
