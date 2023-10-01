package ru.ac.checkpointmanager.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.CheckpointNotFoundException;
import ru.ac.checkpointmanager.exception.EntranceWasAlreadyException;
import ru.ac.checkpointmanager.exception.NoActivePassException;
import ru.ac.checkpointmanager.exception.PassNotFoundException;
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

    @Override //логика сложная, распишу сразу тут, чтоб было понятно
    public Crossing markCrossing(Crossing crossing) {


        //проверяем в бд есть ли такой пропуск или нет, если нет, выбрасываем исключение

        Pass pass = passRepository.findById(crossing.getPass().getId()).orElseThrow(
                () -> new PassNotFoundException("There is no such pass!"));

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
                throw new NoActivePassException(String.format("This %s has not been activated (login to activate)", crossing.getPass().getId()));
            }

            // Если успешно вышли, то переводим статус в CANCELLED
            if (crossing.getDirection().equals(Direction.OUT)) {
                pass.setStatus(PassStatus.COMPLETED);
                passRepository.save(pass);
            }
        }

        Checkpoint checkpoint = checkpointRepository.findById(crossing.getCheckpoint().getId())
                .orElseThrow(() -> new CheckpointNotFoundException("Checkpoint not found"));

        Crossing newCrossing = new Crossing();
        newCrossing.setPass(pass);
        newCrossing.setCheckpoint(checkpoint);
        newCrossing.setLocalDateTime(crossing.getLocalDateTime());
        newCrossing.setDirection(crossing.getDirection());

        return crossingRepository.save(newCrossing);
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

