package ru.ac.checkpointmanager.service.crossing;

import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;

@Service("PERMANENT")
public class PassProcessingPermanent implements PassProcessing {
    @Override
    public void process(Pass pass, Direction direction) {
    }
}