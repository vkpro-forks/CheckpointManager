package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.model.Crossing;

public interface CrossingService {

    Crossing markCrossing(Crossing crossing);
}
