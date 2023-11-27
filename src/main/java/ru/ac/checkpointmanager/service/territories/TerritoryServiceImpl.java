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

    private static final String TERRITORY_NOT_FOUND_MSG = "Territory with id: %s not found";
    private static final String TERRITORY_NOT_FOUND_LOG = "Territory with id: {} not found";


    private final TerritoryRepository territoryRepository;

    private final UserRepository userRepository;

    @Override
    public Territory addTerritory(Territory territory) {
        log.info("Method {}, UUID - {}", MethodLog.getMethodName(), territory.getId());
        trimThemAll(territory);
        return territoryRepository.save(territory);
    }

    @Override
    public Territory findTerritoryById(UUID id) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), id);
        return territoryRepository.findById(id).orElseThrow(
                () -> {
                    log.warn(TERRITORY_NOT_FOUND_LOG, id);
                    return new TerritoryNotFoundException(TERRITORY_NOT_FOUND_MSG.formatted(id));
                });
    }

    @Override
    public List<User> findUsersByTerritoryId(UUID territoryId) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), territoryId);
        findTerritoryById(territoryId);
        List<User> users = territoryRepository.findUsersByTerritoryId(territoryId);
        if (users.isEmpty()) {
            throw new UserNotFoundException(String.format("Users for Territory not found [territory_id=%s]", territoryId));
        }
        return users;
    }

    @Override
    public List<Territory> findTerritoriesByName(String name) {
        log.debug("Method {}, name - {}", MethodLog.getMethodName(), name);
        return territoryRepository.findTerritoriesByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Territory> findAllTerritories() {
        log.debug("Method {}", MethodLog.getMethodName());
        return territoryRepository.findAll();
    }

    @Override
    public Territory updateTerritory(Territory territory) {
        log.info("Method {}, UUID - {}", MethodLog.getMethodName(), territory.getId());
        trimThemAll(territory);
        Territory foundTerritory = territoryRepository.findById(territory.getId())
                .orElseThrow(() -> new TerritoryNotFoundException
                        (String.format("Territory not found [Id=%s]", territory.getId())));

        foundTerritory.setName(territory.getName());
        foundTerritory.setNote(territory.getNote());

        return territoryRepository.save(foundTerritory);
    }

    @Override
    public void attachUserToTerritory(UUID territoryId, UUID userId) {
        log.info("Method {}, user - {}, terr - {}", MethodLog.getMethodName(), userId, territoryId);

        if (territoryRepository.checkUserTerritoryRelation(userId, territoryId)) {
            String message = String.format("User [%s] and territory [%s] are already connected", userId, territoryId);
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        Territory territory = territoryRepository.findById(territoryId).orElseThrow(
                () -> new TerritoryNotFoundException(String.format("Territory not found [Id=%s]", territoryId)));
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User not found [Id=%s]", userId)));

        territory.getUsers().add(user);
        territoryRepository.save(territory);
    }

    @Override
    public void deleteTerritoryById(UUID id) {
        log.info("Method {}, UUID - {}", MethodLog.getMethodName(), id);

        if (territoryRepository.findById(id).isEmpty()) {
            throw new TerritoryNotFoundException(String.format("Territory not found [Id=%s]", id));
        }
        territoryRepository.deleteById(id);
    }

    @Override
    public void detachUserFromTerritory(UUID territoryId, UUID userId) {
        log.info("Method {}, user - {}, terr - {}", MethodLog.getMethodName(), userId, territoryId);

        Territory territory = territoryRepository.findById(territoryId).orElseThrow(
                () -> new TerritoryNotFoundException(String.format("Territory not found [Id=%s]", territoryId)));
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User not found [Id=%s]", userId)));

        if (!territoryRepository.checkUserTerritoryRelation(userId, territoryId)) {
            String message = String.format("User [%s] and territory [%s] have no connection", userId, territoryId);
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        territory.getUsers().remove(user);
        territoryRepository.save(territory);
    }

    @Override
    public Territory findByPassId(UUID passId) {
        return territoryRepository.findByPassId(passId);
    }
}
