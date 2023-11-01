package ru.ac.checkpointmanager.service.crossing;

import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.model.Crossing;

import java.util.UUID;

public interface CrossingService {

    Crossing markCrossing(Crossing crossing);

    Crossing getCrossing(UUID uuid);
}
