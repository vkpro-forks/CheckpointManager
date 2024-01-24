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
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.projection.PassInOutView;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.service.event.PassInOutViewService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PassInOutViewServiceImpl implements PassInOutViewService {

    private final PassRepository passRepository;

    private final UserRepository userRepository;

    private final TerritoryRepository territoryRepository;

    /**
     * Получение событий по идентификатору пользователя
     *
     * @param userId       идентификатор
     * @param pagingParams параметры для постраничной выгрузки
     * @return {@link Page<PassInOutView>} страница с дто событий
     * @throws UserNotFoundException если пользователь не найдет
     */
    @Override
    public Page<PassInOutView> findEventsByUser(UUID userId, PagingParams pagingParams) {
        if (!userRepository.existsById(userId)) {
            log.warn(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
            throw new UserNotFoundException(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
        }
        Pageable pageable = PageRequest.of(pagingParams.getPage(), pagingParams.getSize());
        return passRepository.findEventsByUser(userId, pageable);
    }

    /**
     * Получение событий по идентификатору территории
     *
     * @param terId        идентификатор территории
     * @param pagingParams параметры для постраничной выгрузки
     * @return {@link Page<PassInOutView>} страница с дто событий
     * @throws TerritoryNotFoundException если территория не найдет
     */
    @Override
    public Page<PassInOutView> findEventsByTerritory(UUID terId, PagingParams pagingParams) {
        if (!territoryRepository.existsById(terId)) {
            log.warn(ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(terId));
            throw new TerritoryNotFoundException(ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(terId));
        }
        Pageable pageable = PageRequest.of(pagingParams.getPage(), pagingParams.getSize());

        return passRepository.findEventsByTerritory(terId, pageable);
    }

    /**
     * Получение событий по всем привязанным к пользователю территориям.
     * <p>
     * Если пользователь не найден, генерируется исключение {@link UserNotFoundException}.
     * Затем проверяется, имеет ли пользователь связанные территории. Если нет, выбрасывается {@link TerritoryNotFoundException}.
     * Наконец, поиск событий осуществляется по идентификаторам территорий пользователя с учетом параметров пагинации.
     * </p>
     *
     * @param userId        Идентификатор пользователя, события по территориям которого необходимо найти.
     * @param pagingParams  Параметры пагинации для управления выводом результатов.
     * @return              Страница {@link Page<PassInOutView>} с событиями, соответствующими критериям поиска.
     * @throws UserNotFoundException      Если пользователь с указанным идентификатором не найден.
     * @throws TerritoryNotFoundException Если у пользователя нет связанных территорий.
     */
    @Override
    public Page<PassInOutView> findEventsByUsersTerritories(UUID userId, PagingParams pagingParams) {
        User user = userRepository.findUserWithTerritoriesById(userId).orElseThrow(
                () -> {
                    log.warn(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
                    return new UserNotFoundException(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(userId));
                }
        );
        List<Territory> territories = user.getTerritories();
        if (territories.isEmpty()) {
            log.warn("User [id: {}] has no territories", userId);
            throw new TerritoryNotFoundException(String.format("User [id: %s] has no territories",userId));
        }
        List<UUID> terIds = territories.stream()
                .map(Territory::getId)
                .collect(Collectors.toList());
        Pageable pageable = PageRequest.of(pagingParams.getPage(), pagingParams.getSize());
        return passRepository.findEventsByTerritories(terIds, pageable);
    }

    /**
     * Получение всех событий
     *
     * @param pagingParams параметры для постраничной выгрузки
     * @return {@link Page<PassInOutView>} страница с дто событий
     */
    @Override
    public Page<PassInOutView> findAll(PagingParams pagingParams) {
        Pageable pageable = PageRequest.of(pagingParams.getPage(), pagingParams.getSize());
        return passRepository.findAllEvents(pageable);
    }

}
