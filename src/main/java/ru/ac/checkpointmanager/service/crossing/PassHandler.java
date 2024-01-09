package ru.ac.checkpointmanager.service.crossing;

import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;

public interface PassHandler {

    void handle(Pass pass, Direction currentDirection);

}
