package ru.ac.checkpointmanager.service.crossing;

import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.dto.CrossingRequestDTO;
import ru.ac.checkpointmanager.model.enums.Direction;

import java.util.List;
import java.util.UUID;

public interface CrossingService {

    CrossingDTO addCrossing(CrossingRequestDTO crossingDTO, Direction direction);

    CrossingDTO getCrossing(UUID uuid);

    List<CrossingDTO> getByPassId(UUID passId);
}
