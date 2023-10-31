package ru.ac.checkpointmanager.service.territories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.util.List;
import java.util.UUID;

import static ru.ac.checkpointmanager.utils.StringTrimmer.trimThemAll;

@Service
@Slf4j
@RequiredArgsConstructor
public class TerritoryServiceImpl implements TerritoryService {

    private final TerritoryRepository repository;
    private final UserRepository userRepository;

    @Override
    public Territory addTerritory(Territory territory) {
        log.info("Method {}, UUID - {}", MethodLog.getMethodName(), territory.getId());
        trimThemAll(territory);
        return repository.save(territory);
    }

    @Override
    public Territory findTerritoryById(UUID id) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), id);
        return repository.findById(id).orElseThrow(
                () -> new TerritoryNotFoundException(String.format("Territory not found [id=%s]", id)));
    }

    @Override
    public List<User> findUsersByTerritoryId(UUID territoryId) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), territoryId);
        List<User> users = repository.findUsersByTerritoryId(territoryId);
        if (users.isEmpty()) {
            throw new UserNotFoundException(String.format("Users for Territory not found [territory_id=%s]", territoryId));
        }
        return users;
    }

    @Override
    public List<Territory> findTerritoriesByName(String name) {
        log.debug("Method {}, name - {}", MethodLog.getMethodName(), name);
        return repository.findTerritoriesByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Territory> findAllTerritories() {
        log.debug("Method {}", MethodLog.getMethodName());
        return repository.findAll();
    }

    @Override
    public Territory updateTerritory(Territory territory) {
        log.info("Method {}, UUID - {}", MethodLog.getMethodName(), territory.getId());
        trimThemAll(territory);
        Territory foundTerritory = repository.findById(territory.getId())
                .orElseThrow(() -> new TerritoryNotFoundException
                        (String.format("Territory not found [Id=%s]", territory.getId())));

        foundTerritory.setName(territory.getName());
        foundTerritory.setNote(territory.getNote());

        return repository.save(foundTerritory);
    }

    @Override
    public void attachUserToTerritory(UUID territoryId, UUID userId) {
        log.info("Method {}, user - {}, terr - {}", MethodLog.getMethodName(), userId, territoryId);

        if (repository.checkUserTerritoryRelation(userId, territoryId)) {
            String message = String.format("User [%s] and territory [%s] are already connected", userId, territoryId);
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        Territory territory = repository.findById(territoryId).orElseThrow(
                () -> new TerritoryNotFoundException(String.format("Territory not found [Id=%s]", territoryId)));
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User not found [Id=%s]", userId)));

        territory.getUsers().add(user);
        repository.save(territory);
    }

    @Override
    public void deleteTerritoryById(UUID id) {
        log.info("Method {}, UUID - {}", MethodLog.getMethodName(), id);

        if (repository.findById(id).isEmpty()) {
            throw new TerritoryNotFoundException(String.format("Territory not found [Id=%s]", id));
        }
        repository.deleteById(id);
    }

    @Override
    public void detachUserFromTerritory(UUID territoryId, UUID userId) {
        log.info("Method {}, user - {}, terr - {}", MethodLog.getMethodName(), userId, territoryId);

        if (!repository.checkUserTerritoryRelation(userId, territoryId)) {
            String message = String.format("User [%s] and territory [%s] have no connection", userId, territoryId);
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        Territory territory = repository.findById(territoryId).orElseThrow(
                () -> new TerritoryNotFoundException(String.format("Territory not found [Id=%s]", territoryId)));
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User not found [Id=%s]", userId)));

        territory.getUsers().remove(user);
        repository.save(territory);
    }
}
