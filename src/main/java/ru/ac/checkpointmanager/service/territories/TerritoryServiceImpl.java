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
    private static final String METHOD_CALLED_UUID_LOG = "Method {}, UUID - {}";
    public static final String METHOD_USER_TERR = "Method {}, user - {}, terr - {}";

    private final TerritoryRepository territoryRepository;

    private final UserRepository userRepository;

    @Override
    public Territory addTerritory(Territory territory) {
        log.debug(METHOD_CALLED_UUID_LOG, MethodLog.getMethodName(), territory.getId());
        trimThemAll(territory);
        Territory saved = territoryRepository.save(territory);
        log.info("Territory with [id: {}] was saved", territory.getId());
        return saved;
    }

    @Override
    public Territory findById(UUID id) {
        log.debug(METHOD_CALLED_UUID_LOG, MethodLog.getMethodName(), id);
        return territoryRepository.findById(id).orElseThrow(
                () -> {
                    log.warn(TERRITORY_NOT_FOUND_LOG, id);
                    return new TerritoryNotFoundException(TERRITORY_NOT_FOUND_MSG.formatted(id));
                });
    }

    @Override
    public List<User> findUsersByTerritoryId(UUID territoryId) {
        log.debug(METHOD_CALLED_UUID_LOG, MethodLog.getMethodName(), territoryId);
        findById(territoryId);
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
        UUID territoryId = territory.getId();
        log.debug(METHOD_CALLED_UUID_LOG, MethodLog.getMethodName(), territoryId);
        trimThemAll(territory);
        Territory foundTerritory = territoryRepository.findById(territoryId)
                .orElseThrow(() -> {
                            log.warn(TERRITORY_NOT_FOUND_LOG, territoryId);
                            return new TerritoryNotFoundException(TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
                        }
                );

        foundTerritory.setName(territory.getName());
        foundTerritory.setNote(territory.getNote());

        return territoryRepository.save(foundTerritory);
    }

    @Override
    public void attachUserToTerritory(UUID territoryId, UUID userId) {
        log.debug(METHOD_USER_TERR, MethodLog.getMethodName(), userId, territoryId);
        Territory territory = territoryRepository.findById(territoryId).orElseThrow(
                () -> {
                    log.warn(TERRITORY_NOT_FOUND_LOG, territoryId);
                    return new TerritoryNotFoundException(TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
                });
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException(String.format("User not found [Id=%s]", userId)));
        if (territoryRepository.checkUserTerritoryRelation(userId, territoryId)) {
            String message = String.format("User [%s] and territory [%s] are already connected", userId, territoryId);
            log.warn(message);
            throw new IllegalArgumentException(message);
        }


        territory.getUsers().add(user);
        territoryRepository.save(territory);
    }

    @Override
    public void deleteTerritoryById(UUID id) {
        log.debug(METHOD_CALLED_UUID_LOG, MethodLog.getMethodName(), id);
        if (territoryRepository.findById(id).isEmpty()) {
            log.warn(TERRITORY_NOT_FOUND_LOG, id);
            throw new TerritoryNotFoundException(TERRITORY_NOT_FOUND_MSG.formatted(id));
        }
        log.info("Territory with [id: {}] was successfully deleted", id);
        territoryRepository.deleteById(id);
    }

    @Override
    public void detachUserFromTerritory(UUID territoryId, UUID userId) {
        log.debug(METHOD_USER_TERR, MethodLog.getMethodName(), userId, territoryId);

        Territory territory = territoryRepository.findById(territoryId).orElseThrow(
                () -> {
                    log.warn(TERRITORY_NOT_FOUND_LOG, territoryId);
                    return new TerritoryNotFoundException(TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
                });
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
