package ru.ac.checkpointmanager.service.crossing;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.*;
import ru.ac.checkpointmanager.model.Checkpoint;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.model.passes.PassTypeTime;
import ru.ac.checkpointmanager.repository.CheckpointRepository;
import ru.ac.checkpointmanager.repository.CrossingRepository;
import ru.ac.checkpointmanager.repository.PassRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class CrossingServiceImpl implements CrossingService {

    private final PassRepository passRepository;
    private final CrossingRepository crossingRepository;
    private final CheckpointRepository checkpointRepository;

    @Override
    public Crossing markCrossing(Crossing crossing) {
        log.info("Attempting to mark crossing for pass ID: {}", crossing.getPass().getId());

        Pass pass = validatePass(crossing.getPass().getId());
        Checkpoint checkpoint = validateCheckpoint(crossing.getCheckpoint().getId(), pass.getTerritory().getId());

        Optional<Crossing> lastCrossingOpt = crossingRepository.findTopByPassOrderByIdDesc(pass);
        validateCrossing(crossing.getDirection(), lastCrossingOpt, crossing.getPass().getId());

        manageOneTimePass(crossing.getDirection(), lastCrossingOpt, pass);

        crossing.setPass(pass);
        crossing.setCheckpoint(checkpoint);

        log.info("Local DateTime before setting: {}", crossing.getLocalDateTime());
        crossing.setLocalDateTime(LocalDateTime.now(ZoneId.of("Europe/Moscow")));
        log.info("Local DateTime after setting: {}", crossing.getLocalDateTime());

        log.info("Successfully marked crossing for pass ID: {}", crossing.getPass().getId());
        return crossingRepository.save(crossing);
    }

    @Override
    public Crossing getCrossing(UUID uuid) {
        if (uuid == null) {
            log.warn("Attempt to get Crossing with null UUID");
            throw new IllegalArgumentException("UUID cannot be null");
        }
        Optional<Crossing> personCrossing = crossingRepository.findById(uuid);
        return personCrossing.orElseThrow(() -> {
            log.warn("Crossing not found for UUID: {}", uuid);
            return new CrossingNotFoundException("Crossing not found");
        });
    }


    // проверяет, существует ли пропуск с данным ID и активен ли он
    private Pass validatePass(UUID passId) {
        log.debug("Validating pass with ID: {}", passId);

        return passRepository.findById(passId)
                .filter(p -> p.getStatus() == PassStatus.ACTIVE)
                .filter(p -> {
                    LocalDateTime now = LocalDateTime.now();
                    return now.isAfter(p.getStartTime()) && now.isBefore(p.getEndTime());
                })
                .orElseThrow(() -> new InactivePassException("The pass is not active or not valid for the current time"));
    }


    //проверяет, существует ли КПП с данным ID и принадлежит ли он территории пропуска
    private Checkpoint validateCheckpoint(UUID checkpointId, UUID territoryIdFromPass) {
        log.debug("Validating checkpoint with ID: {}", checkpointId);

        return checkpointRepository.findById(checkpointId)
                .filter(cp -> cp.getTerritory().getId().equals(territoryIdFromPass))
                .orElseThrow(() -> new MismatchedTerritoryException("The checkpoint does not belong to the territory of the pass"));
    }

    //проверяет последнее пересечение для данного пропуска, если оно существует
    //т.е. проверяет, не пытается ли пользователь проехать/пройти два раза в одном и том же направлении
    private void validateCrossing(Direction direction, Optional<Crossing> lastCrossingOpt, UUID passId) {
        log.debug("Validating crossing for pass ID: {} with direction: {}", passId, direction);

        if (lastCrossingOpt.isPresent()) {
            Crossing lastCrossing = lastCrossingOpt.get();

            if (lastCrossing.getDirection().equals(direction)) {
                throw new IllegalStateException(String.format("This passId %s needs to check in/check out", passId));
            }
        }
    }

    //логико для одноразовых пропусков(не был ли уже использован пропуск для въезда, активирован ли пропуск(для случая выезда без предварительного въезда))
    //если направление — выезд, меняет статус пропуска на "завершенный"
    private void manageOneTimePass(Direction currentDirection, Optional<Crossing> lastCrossingOpt, Pass pass) {
        log.debug("Managing one-time pass for pass ID: {}", pass.getId());

        if (pass.getTypeTime() == PassTypeTime.ONETIME) {
            if (isDoubleEntry(currentDirection, lastCrossingOpt)) {
                throw new EntranceWasAlreadyException(String.format("The %s has already been used for entry.", pass.getId()));
            }

            if (isInvalidOutEntry(currentDirection, lastCrossingOpt)) {
                throw new InactivePassException(String.format("This %s has not been activated (login to activate)", pass.getId()));
            }

            if (currentDirection.equals(Direction.OUT)) {
                pass.setStatus(PassStatus.COMPLETED);
            }
        }
    }

    //проверяет, не пытается ли пользователь въехать дважды на территорию с одноразовым пропуском
    private boolean isDoubleEntry(Direction currentDirection, Optional<Crossing> lastCrossingOpt) {
        return lastCrossingOpt
                .filter(lastCrossing -> currentDirection.equals(Direction.IN) && lastCrossing.getDirection().equals(Direction.IN))
                .isPresent();
    }

    //проверяет, не пытается ли пользователь выехать без активации пропуска (т.е. без въезда)
    private boolean isInvalidOutEntry(Direction currentDirection, Optional<Crossing> lastCrossingOpt) {
        return currentDirection == Direction.OUT && lastCrossingOpt.isEmpty();
    }
}

