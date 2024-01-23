package ru.ac.checkpointmanager.service.event;

import org.springframework.data.domain.Page;
import ru.ac.checkpointmanager.dto.passes.PagingParams;
import ru.ac.checkpointmanager.projection.PassInOutView;

import java.util.UUID;

public interface PassInOutViewService {

    Page<PassInOutView> findEventsByUser(UUID userId, PagingParams pagingParams);

    Page<PassInOutView> findEventsByTerritory(UUID terId, PagingParams pagingParams);

}
