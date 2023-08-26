package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.repository.TerritoryRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TerritoryServiceImpl implements TerritoryService {

    private final TerritoryRepository territoryRepository;

    @Override
    public Territory addTerritory(Territory territory) {

        territory.setAddedAt(LocalDate.now());
        return territoryRepository.save(territory);
    }

    @Override
    public Territory findTerritoryById(int id) {
        return territoryRepository.findById(id).orElseThrow(
                () -> new TerritoryNotFoundException(String.format("Room not found [userId=%d]", id)));
    }

    @Override
    public List<Territory> findTerritoriesByName(String name) {
        return territoryRepository.findTerritoriesByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Territory> findAllTerritories() {
        return territoryRepository.findAll();
    }

    @Override
    public Territory updateTerritory(Territory territory) {
        //because "addedAt" field not included in dto and after update territory's data became empty
        //maybe exist better way to save this value in table?
        territory.setAddedAt(territoryRepository
                .findById(territory.getId())
                .get().getAddedAt());
        return territoryRepository.save(territory);
    }

    @Override
    public void deleteTerritoryById(int id) {
        territoryRepository.deleteById(id);
    }


}
