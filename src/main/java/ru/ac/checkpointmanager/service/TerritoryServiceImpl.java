package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.repository.TerritoryRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
    public Territory findTerritoryById(UUID id) {
        return territoryRepository.findById(id).orElseThrow(
                () -> new TerritoryNotFoundException(String.format("Room not found [userId=%s]", id)));
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

        Territory foundTerritory = territoryRepository.findById(territory.getId())
                .orElseThrow(() -> new TerritoryNotFoundException
                        (String.format("Territory not found [Id=%s]", territory.getId())));

        foundTerritory.setName(territory.getName());
        foundTerritory.setNote(territory.getNote());

        return territoryRepository.save(foundTerritory);
    }

    @Override
    public void deleteTerritoryById(UUID id) {
        territoryRepository.deleteById(id);
    }


}
