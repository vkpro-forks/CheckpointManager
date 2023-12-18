package ru.ac.checkpointmanager.service.crossing;

import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.model.enums.Direction;

import java.util.UUID;

public interface CrossingService {

    CrossingDTO addCrossing(CrossingDTO crossingDTO, Direction direction);

    CrossingDTO getCrossing(UUID uuid);
}
