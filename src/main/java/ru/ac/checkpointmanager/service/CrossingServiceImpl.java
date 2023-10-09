package ru.ac.checkpointmanager.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.*;
import ru.ac.checkpointmanager.model.Checkpoint;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.Pass;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.enums.PassStatus;
import ru.ac.checkpointmanager.model.enums.PassTypeTime;
import ru.ac.checkpointmanager.repository.CheckpointRepository;
import ru.ac.checkpointmanager.repository.CrossingRepository;
import ru.ac.checkpointmanager.repository.PassRepository;

import java.util.Optional;
import java.util.UUID;

@Transactional
@Service
@RequiredArgsConstructor
public class CrossingServiceImpl implements CrossingService {


    private final PassRepository passRepository;
    private final CrossingRepository crossingRepository;
    private final CheckpointRepository checkpointRepository;
    private final Logger logger = LoggerFactory.getLogger(CrossingServiceImpl.class);

    @Override //логика сложная, распишу сразу тут, чтоб было понятно
    public Crossing markCrossing(Crossing crossing) {


        //проверяем в бд есть ли такой пропуск или нет, если нет, выбрасываем исключение
        Pass pass = passRepository.findById(crossing.getPass().getId()).orElseThrow(
                () -> new PassNotFoundException("There is no such pass!"));

        //проверяем активен ли пропуск
        if (pass.getStatus() != PassStatus.ACTIVE) {
            throw new InactivePassException("The pass is not active");
        }

        Checkpoint checkpoint = checkpointRepository.findById(crossing.getCheckpoint().getId())
                .orElseThrow(() -> new CheckpointNotFoundException("Checkpoint not found"));

        UUID territoryIdFromPass = pass.getTerritory().getId();
        UUID territoryIdFromCheckpoint = checkpoint.getTerritory().getId();
        if (!territoryIdFromPass.equals(territoryIdFromCheckpoint)) {
            throw new MismatchedTerritoryException("The checkpoint does not belong to the territory of the pass");
        }

        //Используем Optional что дать возможность оставить пустую переменную, чтоб избежать исключения NullPointerException
        //проверяем было ли уже пересечение
        Optional<Crossing> lastCrossingOpt = crossingRepository.findTopByPassOrderByIdDesc(pass);

        validateCrossing(crossing.getDirection(), lastCrossingOpt, crossing.getPass().getId());

        if (pass.getTypeTime() == PassTypeTime.ONETIME) {
            if (lastCrossingOpt.isPresent()) {
                Crossing lastCrossing = lastCrossingOpt.get();

                //вот это сложное условия :))) проверяет, являлось ли последнее пересечение-въездом, если пытаются въехать еще раз, выдаем ошибку
                if (lastCrossing.getDirection().equals(Direction.IN) && crossing.getDirection().equals(Direction.IN)) {
                    throw new EntranceWasAlreadyException(String.format("The %s has already been used for entry.", crossing.getPass().getId()));
                }
            } else if (crossing.getDirection() == Direction.OUT) {
                throw new InactivePassException(String.format("This %s has not been activated (login to activate)", crossing.getPass().getId()));
            }

            if (crossing.getDirection().equals(Direction.OUT)) {
                passRepository.completedStatusById(pass.getId());
            }
        }


        crossing = new Crossing();
        crossing.setPass(pass);
        crossing.setCheckpoint(checkpoint);
        crossing.setLocalDateTime(crossing.getLocalDateTime());
        crossing.setDirection(crossing.getDirection());

        return crossingRepository.save(crossing);
    }


    // Метод для валидации пересечений
    private void validateCrossing(Direction direction, Optional<Crossing> lastCrossingOpt, UUID passId) {
        // Проверяем, есть ли последнее пересечение
        if (lastCrossingOpt.isPresent()) {
            Crossing lastCrossing = lastCrossingOpt.get();

            // Проверяем, чтобы въезд и выезд чередовались
            if (lastCrossing.getDirection().equals(direction)) {
                throw new IllegalStateException(String.format("This passId %s needs to check in/check out", passId));
            }
        }
    }
}

