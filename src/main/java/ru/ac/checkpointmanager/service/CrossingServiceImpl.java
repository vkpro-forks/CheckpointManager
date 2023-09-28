package ru.ac.checkpointmanager.service;

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


    private final PassRepository passRepository;
    private final CrossingRepository crossingRepository;


    @Override //логика сложная, распишу сразу тут, чтоб было понятно
    public Crossing markCrossing(UUID passId, Checkpoint checkpoint, LocalDateTime localDateTime, Direction direction) {
        //проверяем в бд есть ли такой пропуск или нет, если нет, выбрасываем исключение
        Pass pass = passRepository.findById(passId).orElseThrow(
                ()-> new PassNotFoundException("There is no such pass!"));

        //Используем Optional что дать возможность оставить пустую переменную, чтоб избежать исключения NullPointerException
        //проверяем было ли уже пересечение
        Optional<Crossing> lastCrossingOpt = crossingRepository.findTopByPassOrderByIdDesc(pass);

        //проверяем пропуск, проверяем было уже пересечение с этим пропуском
        if (pass.getTypeTime() == PassTypeTime.ONETIME) {
            if (lastCrossingOpt.isPresent()) {
                Crossing lastCrossing = lastCrossingOpt.get();

                //если было пересечение на выезд, выбрасываем исключение, что выезд уже был
                if (lastCrossing.getDirection() == Direction.OUT) {
                    throw new IllegalStateException(String.format("The %s has already been used.", passId));

                    //аналогично, если был въезд, а человек пытается въехать, выбрасываем исключение
                } else if (lastCrossing.getDirection() == Direction.IN) {
                    throw new IllegalStateException(String.format("This %s is not active.", passId));
                }

            //если первое использование пропуска пытаются сделать выезд, выбрасываем исключение, что нужно активировать пропуск - заездом
            } else if (direction == Direction.OUT) {
                throw new IllegalStateException(String.format("This %s has not been activated (login to activate))", passId));
            }

        //если пропуск многоразовый
        } else if (pass.getTypeTime() == PassTypeTime.PERMANENT) {
            if (lastCrossingOpt.isPresent()) {
                Crossing lastCrossing = lastCrossingOpt.get();

                //проверяем чтоб въезд и выезд чередовались между собой, если нет, то так же выкидываем исключение, что нужно сделать активацию - въехать/выехать.
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
