package ru.ac.checkpointmanager.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.PassNotFoundException;
import ru.ac.checkpointmanager.model.Checkpoint;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.Pass;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.enums.PassTypeTime;
import ru.ac.checkpointmanager.repository.CrossingRepository;
import ru.ac.checkpointmanager.repository.PassRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrossingServiceImpl implements CrossingService {

    private PassRepository passRepository;
    private CrossingRepository crossingRepository;


    @Override
    public Crossing markCrossing(UUID passId, Checkpoint checkpoint, LocalDateTime localDateTime, Direction direction) {
        Pass pass = passRepository.findById(passId).orElseThrow(
                ()-> new PassNotFoundException(String.format("There is no such pass %s", passId)));

        Optional<Crossing> lastCrossingOpt = crossingRepository.findTopByPassOrderByIdDesc(pass);

        if (pass.getTypeTime() == PassTypeTime.ONETIME) {
            if (lastCrossingOpt.isPresent()) {
                Crossing lastCrossing = lastCrossingOpt.get();
                if (lastCrossing.getDirection() == Direction.OUT) {
                    throw new IllegalStateException(String.format("The %s has already been used.", passId));
                } else if (lastCrossing.getDirection() == Direction.IN) {
                    throw new IllegalStateException(String.format("This %s is not active.", passId));
                }
            } else if (direction == Direction.OUT) {
                throw new IllegalStateException(String.format("This %s has not been activated (login to activate))", passId));
            }
        } else if (pass.getTypeTime() == PassTypeTime.PERMANENT) {
            if (lastCrossingOpt.isPresent()) {
                Crossing lastCrossing = lastCrossingOpt.get();
                if (lastCrossing.getDirection() == direction) {
                    throw new IllegalStateException(String.format("This %s need to check in/check out", passId));
                }
            }
        }

        Crossing newCrossing = new Crossing();
        newCrossing.setPass(pass);
        newCrossing.setCheckpoint(checkpoint);
        newCrossing.setLocalDateTime(localDateTime);
        newCrossing.setDirection(direction);

        return crossingRepository.save(newCrossing);
    }
}
