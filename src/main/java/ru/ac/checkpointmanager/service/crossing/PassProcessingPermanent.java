package ru.ac.checkpointmanager.service.crossing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;

@Service("PERMANENT")
@Slf4j
public class PassProcessingPermanent implements PassProcessing {
    @Override
    public void process(Pass pass, Direction direction) {
        log.debug("Processing permanent pass [{}]", pass.getId());

        if (pass.getCrossings().get(0).getDirection() == direction) {
            log.warn("Two crossings in a row in the same direction - {}, pass {}", direction, pass.getId());
        }
    }
}