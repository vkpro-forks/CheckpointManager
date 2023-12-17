package ru.ac.checkpointmanager.service.crossing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.EntranceWasAlreadyException;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassStatus;

@Service("ONETIME")
@Slf4j
@RequiredArgsConstructor
public class PassProcessingOnetime implements PassProcessing {

    private static final String PASS_STATUS_CHANGED_LOG = "Pass [{}], changed status on {}";
    private static final String PASS_ALREADY_USED = "OnetimePass [%s] has already been used, it is not possible to enter";

    @Override
    public void process(Pass pass, Direction direction) {
        log.debug("Processing onetime pass [{}]", pass.getId());

        if (direction == Direction.OUT) {
            pass.setStatus(PassStatus.COMPLETED);
            log.info(PASS_STATUS_CHANGED_LOG, pass.getId(), pass.getStatus());
        } else if (!pass.getCrossings().isEmpty()) {
            log.warn(PASS_ALREADY_USED.formatted(pass.getId()));
            throw new EntranceWasAlreadyException(PASS_ALREADY_USED.formatted(pass.getId()));
        }
    }
}