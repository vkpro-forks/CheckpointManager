package ru.ac.checkpointmanager.service.territories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
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

    private static final String METHOD_CALLED_UUID_LOG = "Method {}, UUID - {}";
    public static final String METHOD_USER_TERR = "Method {}, user - {}, terr - {}";

    private final TerritoryRepository territoryRepository;
    private final UserRepository userRepository;
    private final TerritoryMapper territoryMapper;
    private final UserMapper userMapper;


    @Override
    @Transactional
    public TerritoryDTO addTerritory(TerritoryDTO territoryDTO) {
        log.debug(METHOD_CALLED_UUID_LOG, MethodLog.getMethodName(), territoryDTO.getId());
        Territory territory = territoryMapper.toTerritory(territoryDTO);
        trimThemAll(territory);
        Territory saved = territoryRepository.save(territory);
        log.info("Territory with [id: {}] was saved", territory.getId());
        return territoryMapper.toTerritoryDTO(saved);
    }

    @Override
    public TerritoryDTO findById(UUID territoryId) {
        log.debug(METHOD_CALLED_UUID_LOG, MethodLog.getMethodName(), territoryId);
        Territory territory = territoryRepository.findById(territoryId).orElseThrow(
                () -> {
                    log.warn(ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
                    return new TerritoryNotFoundException(ExceptionUtils.TERRITORY_NOT_FOUND_MSG
                            .formatted(territoryId));
                });
        return territoryMapper.toTerritoryDTO(territory);
    }

    @Override
    public Territory findTerritoryById(UUID territoryId) {
        return territoryRepository.findById(territoryId).orElseThrow(
                () -> {
                    log.warn(ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
                    return new TerritoryNotFoundException(ExceptionUtils.TERRITORY_NOT_FOUND_MSG
                            .formatted(territoryId));
                });
    }

    @Override
    public List<UserResponseDTO> findUsersByTerritoryId(UUID territoryId) {
        log.debug(METHOD_CALLED_UUID_LOG, MethodLog.getMethodName(), territoryId);
        findById(territoryId);
        List<User> users = territoryRepository.findUsersByTerritoryId(territoryId);
        return userMapper.toUserResponseDTOs(users);
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
    @Transactional
    public TerritoryDTO updateTerritory(TerritoryDTO territoryDTO) {
        UUID territoryId = territoryDTO.getId();
        log.debug(METHOD_CALLED_UUID_LOG, MethodLog.getMethodName(), territoryId);
        trimThemAll(territoryDTO);
        Territory foundTerritory = territoryRepository.findById(territoryId).orElseThrow(
                () -> {
                    log.warn(ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
                    return new TerritoryNotFoundException(ExceptionUtils.TERRITORY_NOT_FOUND_MSG
                            .formatted(territoryId));
                });

        foundTerritory.setName(territoryDTO.getName());
        foundTerritory.setNote(territoryDTO.getNote());

        territoryRepository.save(foundTerritory);
        return territoryMapper.toTerritoryDTO(foundTerritory);
    }

    @CacheEvict(value = "user-territory", key = "#userId")
    @Override
    public void attachUserToTerritory(UUID territoryId, UUID userId) {
        log.debug(METHOD_USER_TERR, MethodLog.getMethodName(), userId, territoryId);
        Territory territory = territoryRepository.findById(territoryId).orElseThrow(
                () -> {
                    log.warn(ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
                    return new TerritoryNotFoundException(ExceptionUtils.TERRITORY_NOT_FOUND_MSG
                            .formatted(territoryId));
                });
        User user = userRepository.findById(userId).orElseThrow(
                () -> {
                    log.warn(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
                    return new UserNotFoundException(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
                });

        if (territoryRepository.checkUserTerritoryRelation(userId, territoryId)) {
            String message = String.format("User [%s] and territory [%s] are already connected", userId, territoryId);
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        user.getTerritories().add(territory);
        userRepository.save(user);
    }


    @Override
    public void deleteTerritoryById(UUID territoryId) {
        log.debug(METHOD_CALLED_UUID_LOG, MethodLog.getMethodName(), territoryId);
        if (territoryRepository.findById(territoryId).isEmpty()) {
            log.warn(ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
            throw new TerritoryNotFoundException(ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
        }
        log.info("Territory with [id: {}] was successfully deleted", territoryId);
        territoryRepository.deleteById(territoryId);
    }

    @CacheEvict(value = "user-territory", key = "#userId")
    @Override
    public void detachUserFromTerritory(UUID territoryId, UUID userId) {
        log.debug(METHOD_USER_TERR, MethodLog.getMethodName(), userId, territoryId);

        Territory territory = territoryRepository.findById(territoryId).orElseThrow(
                () -> {
                    log.warn(ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(territoryId));
                    return new TerritoryNotFoundException(ExceptionUtils.TERRITORY_NOT_FOUND_MSG
                            .formatted(territoryId));
                });
        User user = userRepository.findById(userId).orElseThrow(
                () -> {
                    log.warn(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
                    return new UserNotFoundException(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
                });
        if (!territoryRepository.checkUserTerritoryRelation(userId, territoryId)) {
            String message = String.format("User [%s] and territory [%s] have no connection", userId, territoryId);
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        user.getTerritories().remove(territory);
        userRepository.save(user);
    }

    @Override
    public Territory findByPassId(UUID passId) {
        return territoryRepository.findByPassId(passId);
    }

}
