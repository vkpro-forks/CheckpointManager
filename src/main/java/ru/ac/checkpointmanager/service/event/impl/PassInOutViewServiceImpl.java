package ru.ac.checkpointmanager.service.event.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.dto.passes.PagingParams;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.projection.PassInOutViewProjection;
import ru.ac.checkpointmanager.repository.PassInOutViewRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.service.event.PassInOutViewService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PassInOutViewServiceImpl implements PassInOutViewService {

    private final PassInOutViewRepository passInOutViewRepository;

    private final UserRepository userRepository;

    private final TerritoryRepository territoryRepository;

    @Override
    public Page<PassInOutViewProjection> findEventsByUser(UUID userId, PagingParams pagingParams) {
        if (!userRepository.existsById(userId)) {
            log.warn(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
            throw new UserNotFoundException(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
        }
        Pageable pageable = PageRequest.of(pagingParams.getPage(), pagingParams.getSize());
        return passInOutViewRepository.findEventsByUser(userId, pageable);
    }

    @Override
    public Page<PassInOutViewProjection> findEventsByTerritory(UUID terId, PagingParams pagingParams) {
        if (!territoryRepository.existsById(terId)) {
            log.warn(ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(terId));
            throw new TerritoryNotFoundException(ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(terId));
        }
        Pageable pageable = PageRequest.of(pagingParams.getPage(), pagingParams.getSize());

        return passInOutViewRepository.findEventsByTerritory(terId, pageable);
    }
}
