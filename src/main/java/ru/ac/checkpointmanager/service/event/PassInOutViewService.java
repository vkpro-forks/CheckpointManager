package ru.ac.checkpointmanager.service.event;

import org.springframework.data.domain.Page;
import ru.ac.checkpointmanager.dto.passes.PagingParams;
import ru.ac.checkpointmanager.projection.PassInOutViewProjection;

import java.util.UUID;

public interface PassInOutViewService {

    Page<PassInOutViewProjection> findEventsByUser(UUID userId, PagingParams pagingParams);

    Page<PassInOutViewProjection> findEventsByTerritory(UUID terId, PagingParams pagingParams);

}
