package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TerritoryServiceImpl implements TerritoryService {

    private final TerritoryRepository territoryRepository;
    private final UserRepository userRepository;

    @Override
    public Territory addTerritory(Territory territory) {

        territory.setAddedAt(LocalDate.now());
        return territoryRepository.save(territory);
    }

    @Override
    public Territory findTerritoryById(UUID id) {
        return territoryRepository.findById(id).orElseThrow(
                () -> new TerritoryNotFoundException(String.format("Territory not found [id=%s]", id)));
    }

    @Override
    public List<User> findUsersByTerritoryId(UUID territoryId) {
        List<User> users = territoryRepository.findUsersByTerritoryId(territoryId);
        if (users.isEmpty()) {
            throw new UserNotFoundException(String.format("Users for Territory not found [territory_id=%s]", territoryId));
        }
        return users;
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
    public void attachUserToTerritory(UUID territoryId, UUID userId) {

        Territory territory = territoryRepository.findById(territoryId).orElseThrow(
                () -> new TerritoryNotFoundException(String.format("Territory not found [Id=%s]", territoryId)));
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User not found [Id=%s]", userId)));

        territory.getUsers().add(user);
        territoryRepository.save(territory);
    }

    @Override
    public void deleteTerritoryById(UUID id) {

        if (territoryRepository.findById(id).isEmpty()) {
            throw new TerritoryNotFoundException(String.format("Territory not found [Id=%s]", id));
        }
        territoryRepository.deleteById(id);
    }

    @Override
    public void detachUserFromTerritory(UUID territoryId, UUID userId) {

        Territory territory = territoryRepository.findById(territoryId).orElseThrow(
                () -> new TerritoryNotFoundException(String.format("Territory not found [Id=%s]", territoryId)));
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User not found [Id=%s]", userId)));

        territory.getUsers().remove(user);
        territoryRepository.save(territory);
    }
}
