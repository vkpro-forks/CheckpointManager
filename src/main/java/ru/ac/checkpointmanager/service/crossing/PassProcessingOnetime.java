package ru.ac.checkpointmanager.service.crossing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.EntranceWasAlreadyException;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassStatus;

@Service("ONETIME")
@RequiredArgsConstructor
public class PassProcessingOnetime implements PassProcessing {

    @Override
    public void process(Pass pass, Direction direction) {

        if (direction == Direction.OUT) {
            pass.setStatus(PassStatus.COMPLETED);
        } else if (pass.getCrossings().size() > 0) {
            throw new EntranceWasAlreadyException("Pass [%s] has already been used".formatted(pass.getId()));
        }
    }
}