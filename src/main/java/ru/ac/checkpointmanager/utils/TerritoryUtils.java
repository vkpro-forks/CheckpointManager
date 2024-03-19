package ru.ac.checkpointmanager.utils;

import lombok.extern.slf4j.Slf4j;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class TerritoryUtils {

    /**
     * Получает список идентификаторов территорий для указанного пользователя.
     * Если у пользователя нет связанных территорий, выбрасывает исключение TerritoryNotFoundException.
     *
     * @param user объект пользователя, территории которого необходимо получить.
     * @param userId идентификатор пользователя, используется для формирования сообщения об ошибке.
     * @return список UUID, представляющих идентификаторы территорий пользователя.
     * @throws TerritoryNotFoundException если у пользователя нет связанных территорий.
     */
    public static List<UUID> getTerritoryIdsOrThrow(User user, UUID userId) {
        List<Territory> territories = user.getTerritories();
        if (territories.isEmpty()) {
            throw new TerritoryNotFoundException(ExceptionUtils.USER_TERRITORY_NOT_FOUND_MSG.formatted(userId));
        }
        return territories.stream()
                .map(Territory::getId)
                .collect(Collectors.toList());
    }

    private TerritoryUtils() {

    }
}
