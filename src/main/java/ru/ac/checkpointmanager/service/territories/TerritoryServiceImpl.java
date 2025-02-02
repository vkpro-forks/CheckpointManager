package ru.ac.checkpointmanager.service.territories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.TerritoryUpdateDTO;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.passes.PagingParams;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserTerritoryRelationException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.TerritoryMapper;
import ru.ac.checkpointmanager.mapper.UserMapper;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.utils.StringTrimmer;

import java.util.List;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class TerritoryServiceImpl implements TerritoryService {

    private final TerritoryRepository territoryRepository;
    private final UserRepository userRepository;
    private final TerritoryMapper territoryMapper;
    private final UserMapper userMapper;


    @Override
    @Transactional
    public TerritoryDTO addTerritory(TerritoryDTO territoryDTO) {
        Territory territory = territoryMapper.toTerritory(territoryDTO);
        StringTrimmer.trimThemAll(territory);
        Territory saved = territoryRepository.save(territory);
        log.info("Territory with [id: {}] was saved", territory.getId());
        return territoryMapper.toTerritoryDTO(saved);
    }

    @Override
    public TerritoryDTO findById(UUID territoryId) {
        Territory territory = territoryRepository.findById(territoryId)
                .orElseThrow(() -> new TerritoryNotFoundException(territoryId));
        return territoryMapper.toTerritoryDTO(territory);
    }

    @Override
    public Territory findTerritoryById(UUID territoryId) {
        return territoryRepository.findById(territoryId)
                .orElseThrow(() -> new TerritoryNotFoundException(territoryId));
    }

    @Override
    public Page<UserResponseDTO> findUsersByTerritoryId(UUID territoryId, PagingParams pagingParams) {
        findById(territoryId);
        Pageable pageable = PageRequest.of(pagingParams.getPage(), pagingParams.getSize());
        Page<User> userPage = userRepository.findUsersByTerritoryId(territoryId, pageable);
        return userPage.map(userMapper::toUserResponseDTO);
    }

    @Override
    public List<TerritoryDTO> findTerritoriesByName(String name) {
        List<Territory> territories = territoryRepository.findTerritoriesByNameContainingIgnoreCase(name);
        return territoryMapper.toTerritoriesDTO(territories);
    }

    @Override
    public List<TerritoryDTO> findAllTerritories() {
        List<Territory> territories = territoryRepository.findAll();
        return territoryMapper.toTerritoriesDTO(territories);
    }

    @Override
    @Transactional
    public TerritoryDTO updateTerritory(TerritoryUpdateDTO territoryDTO) {
        UUID territoryId = territoryDTO.getId();
        StringTrimmer.trimThemAll(territoryDTO);
        Territory foundTerritory = territoryRepository.findById(territoryId)
                .orElseThrow(() -> new TerritoryNotFoundException(territoryId));

        if (territoryDTO.getName() != null) foundTerritory.setName(territoryDTO.getName());
        if (territoryDTO.getNote() != null) foundTerritory.setNote(territoryDTO.getNote());
        if (territoryDTO.getCity() != null) foundTerritory.setCity(territoryDTO.getCity());
        if (territoryDTO.getAddress() != null) foundTerritory.setAddress(territoryDTO.getAddress());

        territoryRepository.save(foundTerritory);
        return territoryMapper.toTerritoryDTO(foundTerritory);
    }

    @CacheEvict(value = "user-territory", key = "#userId")
    @Override
    public void attachUserToTerritory(UUID territoryId, UUID userId) {
        Territory territory = territoryRepository.findById(territoryId)
                .orElseThrow(() -> new TerritoryNotFoundException(territoryId));

        User user = userRepository.findById(userId).orElseThrow(
                () -> {
                    log.warn(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
                    return new UserNotFoundException(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
                });

        if (territoryRepository.checkUserTerritoryRelation(userId, territoryId)) {
            String message = String.format("User [%s] and territory [%s] are already connected", userId, territoryId);
            log.warn(message);
            throw new UserTerritoryRelationException(message);
        }

        user.getTerritories().add(territory);
        userRepository.save(user);
    }


    @Override
    public void deleteTerritoryById(UUID territoryId) {
        if (territoryRepository.findById(territoryId).isEmpty()) {
            throw new TerritoryNotFoundException(territoryId);
        }
        log.info("Territory with [id: {}] was successfully deleted", territoryId);
        territoryRepository.deleteById(territoryId);
    }

    @CacheEvict(value = "user-territory", key = "#userId")
    @Override
    public void detachUserFromTerritory(UUID territoryId, UUID userId) {

        Territory territory = territoryRepository.findById(territoryId)
                .orElseThrow(() -> new TerritoryNotFoundException(territoryId));

        User user = userRepository.findById(userId).orElseThrow(
                () -> {
                    log.warn(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
                    return new UserNotFoundException(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
                });
        if (!territoryRepository.checkUserTerritoryRelation(userId, territoryId)) {
            String message = String.format("User [%s] and territory [%s] have no connection", userId, territoryId);
            log.warn(message);
            throw new UserTerritoryRelationException(message);
        }

        user.getTerritories().remove(territory);
        userRepository.save(user);
    }

    @Override
    public Territory findByPassId(UUID passId) {
        return territoryRepository.findByPassId(passId);
    }

}
