package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.PassNotFoundException;
import ru.ac.checkpointmanager.model.Checkpoint;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.Pass;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.enums.PassStatus;
import ru.ac.checkpointmanager.model.enums.PassTypeTime;
import ru.ac.checkpointmanager.repository.CrossingRepository;
import ru.ac.checkpointmanager.repository.PassRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrossingServiceImpl implements CrossingService {


    private final PassRepository passRepository;
    private final CrossingRepository crossingRepository;

    @Override //логика сложная, распишу сразу тут, чтоб было понятно
    public Crossing markCrossing(UUID passId, Checkpoint checkpoint, LocalDateTime localDateTime, Direction direction) {
        //проверяем в бд есть ли такой пропуск или нет, если нет, выбрасываем исключение
        Pass pass = passRepository.findById(passId).orElseThrow(
                () -> new PassNotFoundException("There is no such pass!"));

        //Используем Optional что дать возможность оставить пустую переменную, чтоб избежать исключения NullPointerException
        //проверяем было ли уже пересечение
        Optional<Crossing> lastCrossingOpt = crossingRepository.findTopByPassOrderByIdDesc(pass);

        validateCrossing(direction, lastCrossingOpt, passId);

        if (pass.getTypeTime() == PassTypeTime.ONETIME) {
            if (lastCrossingOpt.isPresent()) {
                Crossing lastCrossing = lastCrossingOpt.get();

                //вот это сложное условия :))) проверяет, являлось ли последнее пересечение-въездом, если пытаются въехать еще раз, выдаем ошибку
                if (lastCrossing.getDirection().equals(Direction.IN) && direction.equals(Direction.IN)) {
                    throw new IllegalStateException(String.format("The %s has already been used for entry.", passId));
                }
            } else if (direction == Direction.OUT) {
                throw new IllegalStateException(String.format("This %s has not been activated (login to activate)", passId));
            }

            // Если успешно вышли, то переводим статус в CANCELLED
            if (direction.equals(Direction.OUT)) {
                pass.setStatus(PassStatus.CANCELLED);
                passRepository.save(pass);
            }
        }

        Crossing newCrossing = new Crossing();
        newCrossing.setPass(pass);
        newCrossing.setCheckpoint(checkpoint);
        newCrossing.setLocalDateTime(localDateTime);
        newCrossing.setDirection(direction);

        return crossingRepository.save(newCrossing);
    }


    // Метод для валидации пересечений
    private void validateCrossing(Direction direction, Optional<Crossing> lastCrossingOpt, UUID passId) {
        // Проверяем, есть ли последнее пересечение
        if (lastCrossingOpt.isPresent()) {
            Crossing lastCrossing = lastCrossingOpt.get();

            // Проверяем, чтобы въезд и выезд чередовались
            if (lastCrossing.getDirection().equals(direction)) {
                throw new IllegalStateException(String.format("This %s needs to check in/check out", passId));
            }
        }
    }
}
