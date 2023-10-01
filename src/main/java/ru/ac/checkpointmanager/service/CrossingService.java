package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.model.Checkpoint;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.enums.Direction;

import java.time.LocalDateTime;
import java.util.UUID;

public interface CrossingService {

    Crossing markCrossing(Crossing crossing);
}
