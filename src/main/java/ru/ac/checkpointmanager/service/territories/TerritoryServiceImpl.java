package ru.ac.checkpointmanager.service.territories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.TerritoryMapper;
import ru.ac.checkpointmanager.mapper.UserMapper;
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

    private static final String USER_NOT_FOUND_LOG = "User with [id: {}] not found";
    private static final String USER_NOT_FOUND_MSG = "User with id: %s not found";
    private static final String METHOD_CALLED_UUID_LOG = "Method {}, UUID - {}";
    public static final String METHOD_USER_TERR = "Method {}, user - {}, terr - {}";

    private final TerritoryRepository territoryRepository;
    private final UserRepository userRepository;
    private final TerritoryMapper territoryMapper;
    private final UserMapper userMapper;


    @Override
    public TerritoryDTO addTerritory(TerritoryDTO territoryDTO) {
        log.debug(METHOD_CALLED_UUID_LOG, MethodLog.getMethodName(), territoryDTO.getId());
        Territory territory = territoryMapper.toTerritory(territoryDTO);
        trimThemAll(territory);
        Territory saved = territoryRepository.save(territory);
        log.info("Territory with [id: {}] was saved", territory.getId());
        return territoryMapper.toTerritoryDTO(saved);
    }

    @Override
    public TerritoryDTO findById(UUID id) {
        log.debug(METHOD_CALLED_UUID_LOG, MethodLog.getMethodName(), id);
        Territory territory = territoryRepository.findById(id).orElseThrow(
                () -> {
                    log.warn(TERRITORY_NOT_FOUND_LOG, id);
                    return new TerritoryNotFoundException(TERRITORY_NOT_FOUND_MSG.formatted(id));
                });
        return territoryMapper.toTerritoryDTO(territory);
    }

    @Override
    public Territory findTerritoryById(UUID id) {
        return territoryRepository.findById(id).orElseThrow(
                () -> {
                    log.warn(TERRITORY_NOT_FOUND_LOG, id);
                    return new TerritoryNotFoundException(TERRITORY_NOT_FOUND_MSG.formatted(id));
                });
    }

    @Override
    public List<UserResponseDTO> findUsersByTerritoryId(UUID territoryId) {
        log.debug(METHOD_CALLED_UUID_LOG, MethodLog.getMethodName(), territoryId);
        findById(territoryId);
        return territoryRepository.findUsersByTerritoryId(territoryId);
    }

    @Override
    public List<TerritoryDTO> findTerritoriesByName(String name) {
        log.debug("Method {}, name - {}", MethodLog.getMethodName(), name);
        List<Territory> territories = territoryRepository.findTerritoriesByNameContainingIgnoreCase(name);
        return territoryMapper.toTerritoriesDTO(territories);
    }

    @Override
    public List<TerritoryDTO> findAllTerritories() {
        log.debug("Method {}", MethodLog.getMethodName());
        List<Territory> territories = territoryRepository.findAll();
        return territoryMapper.toTerritoriesDTO(territories);
    }

    @Override
    public TerritoryDTO updateTerritory(TerritoryDTO territoryDTO) {
        UUID territoryId = territoryDTO.getId();
        log.debug(METHOD_CALLED_UUID_LOG, MethodLog.getMethodName(), territoryId);
        trimThemAll(territoryDTO);
        Territory foundTerritory = territoryRepository.findById(territoryId)
                .orElseThrow(() -> {
                            log.warn(TERRITORY_NOT_FOUND_LOG, territoryId);
                            return new TerritoryNotFoundException(TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
                        }
                );

        foundTerritory.setName(territoryDTO.getName());
        foundTerritory.setNote(territoryDTO.getNote());

        territoryRepository.save(foundTerritory);
        return territoryMapper.toTerritoryDTO(foundTerritory);
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
                () -> {
                    log.warn(USER_NOT_FOUND_LOG, userId);
                    return new UserNotFoundException(USER_NOT_FOUND_MSG.formatted(userId));
                }
        );
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
                () -> {
                    log.warn(USER_NOT_FOUND_LOG, userId);
                    return new UserNotFoundException(USER_NOT_FOUND_MSG.formatted(userId));
                }
        );
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
