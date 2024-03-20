package ru.ac.checkpointmanager.service.crossing;

import org.springframework.data.domain.Page;
import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.dto.CrossingRequestDTO;
import ru.ac.checkpointmanager.dto.passes.PagingParams;
import ru.ac.checkpointmanager.model.enums.Direction;

import java.util.UUID;

public interface CrossingService {

    CrossingDTO addCrossing(CrossingRequestDTO crossingDTO, Direction direction);

    CrossingDTO getCrossing(UUID uuid);

    Page<CrossingDTO> getByPassId(UUID passId, PagingParams pagingParams);
}
