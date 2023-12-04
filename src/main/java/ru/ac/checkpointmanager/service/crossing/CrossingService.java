package ru.ac.checkpointmanager.service.crossing;

import ru.ac.checkpointmanager.dto.CrossingDTO;

import java.util.UUID;

public interface CrossingService {

    CrossingDTO addCrossing(CrossingDTO crossingDTO);

    CrossingDTO getCrossing(UUID uuid);
}
