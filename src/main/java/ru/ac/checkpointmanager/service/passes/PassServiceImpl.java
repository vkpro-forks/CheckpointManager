package ru.ac.checkpointmanager.service.passes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.exception.PassNotFoundException;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.Person;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.model.passes.PassWalk;
import ru.ac.checkpointmanager.repository.CrossingRepository;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.service.person.PersonService;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.ac.checkpointmanager.utils.StringTrimmer.trimThemAll;

/**
 * Service class for Pass domain objects
 *
 * @author Dmitry Ldv236
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PassServiceImpl implements PassService{

    private final PassRepository repository;
    private final CrossingRepository crossingRepository;

    private int hourForLogInScheduledCheck;

    @Override
    public Pass addPass(Pass pass) {
        log.info("Method {}, UUID - {}", MethodLog.getMethodName(), pass.getId());

        checkUserTerritoryRelation(pass);
        checkOverlapTime(pass);

        trimThemAll(pass);
        pass.setStatus(PassStatus.ACTIVE);


        return repository.save(pass);
    }

    @Override
    public List<Pass> findPasses() {
        log.debug("Method {}", MethodLog.getMethodName());
        return repository.findAll();
    }

    @Override
    public Pass findPass(UUID id) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), id);
        return repository.findById(id).orElseThrow(
                () -> new PassNotFoundException(String.format("Pass not found [userId=%s]", id)));
    }

    @Override
    public List<Pass> findPassesByUser(UUID userId) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), userId);
        List<Pass> foundPasses = repository.findPassesByUserIdOrderByAddedAtDesc(userId);

        if (foundPasses.isEmpty()) {
            throw new PassNotFoundException(String.format("For User [id=%s] not exist any Passes", userId));
        }
        return foundPasses;
    }

    @Override
    public List<Pass> findPassesByTerritory(UUID terId) {
        log.debug("Method {}, UUID - {}", MethodLog.getMethodName(), terId);
        List<Pass> foundPasses = repository.findPassesByTerritoryIdOrderByAddedAtDesc(terId);

        if (foundPasses.isEmpty()) {
            throw new PassNotFoundException(String.format("For Territory [id=%s] not exist any Passes", terId));
        }
        return foundPasses;
    }

    @Override
    public Pass updatePass(Pass pass) {
        log.info("Method {}, UUID - {}", MethodLog.getMethodName(), pass.getId());
        checkUserTerritoryRelation(pass);
        checkOverlapTime(pass);

        trimThemAll(pass);
        Pass foundPass = findPass(pass.getId());

        if (!foundPass.getStatus().equals(PassStatus.ACTIVE)) {
            throw new IllegalStateException("This pass is not active, it cannot be changed. " +
                    "You can only change the active pass");
        }

        foundPass.setName(pass.getName());
        foundPass.setTypeTime(pass.getTypeTime());
        foundPass.setTerritory(pass.getTerritory());
        foundPass.setNote(pass.getNote());
        foundPass.setStartTime(pass.getStartTime());
        foundPass.setEndTime(pass.getEndTime());

        return repository.save(foundPass);
    }

    @Override
    @Transactional
    public Pass cancelPass(UUID id) {
        log.info("Method {}, UUID - {}", MethodLog.getMethodName(), id);
        Pass pass = findPass(id);

        if (!pass.getStatus().equals(PassStatus.ACTIVE)) {
            throw new IllegalStateException("You can only cancel an active Pass");
        }

        if (crossingRepository.findCrossingsByPassId(id).size() > 0) {
            pass.setStatus(PassStatus.COMPLETED);
            return pass;
        }

        pass.setStatus(PassStatus.CANCELLED);
        return pass;
    }

    @Override
    public Pass activateCancelledPass(UUID id) {
        log.info("Method {}, UUID - {}", MethodLog.getMethodName(), id);
        Pass pass = findPass(id);

        if (!pass.getStatus().equals(PassStatus.CANCELLED)) {
            throw new IllegalStateException("You can only activate a previously cancelled pass");
        }

        if (pass.getEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("This pass has already expired");
        }

        pass.setStatus(PassStatus.ACTIVE);
        return pass;
    }

    @Override
    public Pass unWarningPass(UUID id) {
        log.info("Method {}, UUID - {}", MethodLog.getMethodName(), id);
        Pass pass = findPass(id);

        if (!pass.getStatus().equals(PassStatus.WARNING)) {
            throw new IllegalStateException("You can only to unwarnining a previously warninged pass");
        }

        pass.setStatus(PassStatus.COMPLETED);
        return pass;
    }

    @Override
    public void deletePass(UUID id) {
        log.info("Method {}, UUID - {}", MethodLog.getMethodName(), id);
        if (repository.findById(id).isEmpty()) {
            throw new PassNotFoundException(String.format("Pass not found [Id=%s]", id));
        }
        repository.deleteById(id);
    }

    /**
     * @param newPass добавляемый или изменяемый пропуск
     * @exception TerritoryNotFoundException, если указанный юзер не имеет связи с указанной территорией,
     * т.е. не имеет права создавать пропуска для этой территории
     */
    private void checkUserTerritoryRelation(Pass newPass) {
        UUID userId = newPass.getUser().getId();
        UUID territoryId = newPass.getTerritory().getId();
        if (!repository.checkUserTerritoryRelation(userId, territoryId)) {
            String message = String.format("Reject operation: user [%s] not have permission to create passes " +
                    "for this territory [%s]", userId, territoryId);
            log.warn(message);
            throw new IllegalArgumentException(message);
        }
    }

//    /**
//     * @param newPass добавляемый или изменяемый пропуск
//     * @exception IllegalArgumentException, если в системе существует другой активный пропуск,
//     * созданный тем же юзером, в котором совпадает территория, данные машины/человека
//     * и пересекается (накладывается) время действия
//     */
    /**
     * Проверяет, не пересекается ли время нового пропуска с существующими пропусками пользователя.
     * Если найден пропуск с пересекающимся временем, будет выброшено исключение IllegalArgumentException.
     *
     * @param newPass Новый пропуск для проверки.
     * @throws IllegalArgumentException если найден пропуск с пересекающимся временем.
     */
    void checkOverlapTime(Pass newPass) {
        List<Pass> passesByUser = repository.findPassesByUserIdOrderByAddedAtDesc(newPass.getUser().getId());
        List<Pass> filteredPassesByUser = filterPassesByType(passesByUser, newPass.getClass());

        Optional<Pass> overlapPass = findOverlappingPass(filteredPassesByUser, newPass);

        if (overlapPass.isPresent()) {
            String message = String.format("Reject operation: user [%s] already has such a pass with " +
                    "overlapping time [%s]", newPass.getUser().getId(), overlapPass.get().getId());
            log.debug(message);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Фильтрует список пропусков по указанному типу.
     *
     * @param passes Список пропусков для фильтрации.
     * @param type   Тип пропуска для фильтрации.
     * @return Отфильтрованный список пропусков указанного типа.
     */
    List<Pass> filterPassesByType(List<Pass> passes, Class<?> type) {
        return passes.stream()
                .filter(pass -> pass.getClass().equals(type))
                .toList();
    }

    /**
     * Ищет пропуск, пересекающийся по времени с новым пропуском.
     *
     * @param passes  Список пропусков для проверки.
     * @param newPass Новый пропуск для проверки.
     * @return Optional, содержащий пересекающийся пропуск, если таковой найден.
     */
    Optional<Pass> findOverlappingPass(List<Pass> passes, Pass newPass) {
        return passes.stream()
                .filter(existPass -> hasSameIdentifier(newPass, existPass))
                .filter(existPass -> Objects.equals(existPass.getStatus(), PassStatus.ACTIVE))
                .filter(existPass -> !Objects.equals(newPass.getId(), existPass.getId()))
                .filter(existPass -> Objects.equals(newPass.getTerritory(), existPass.getTerritory()))
                .filter(existPass -> newPass.getEndTime().isAfter(existPass.getStartTime()) &&
                        newPass.getStartTime().isBefore(existPass.getEndTime()))
                .findFirst();
    }

    /**
     * Проверяет, имеют ли два пропуска один и тот же идентификатор (например, номер автомобиля или имя человека).
     *
     * @param newPass   Новый пропуск для проверки.
     * @param existPass Существующий пропуск для проверки.
     * @return true, если пропуски имеют одинаковые идентификаторы, иначе false.
     */
    boolean hasSameIdentifier(Pass newPass, Pass existPass) {
        if (existPass instanceof PassAuto) {
            return Objects.equals(((PassAuto) newPass).getCar().getLicensePlate(),
                    ((PassAuto) existPass).getCar().getLicensePlate());
        } else if (existPass instanceof PassWalk) {
            return Objects.equals(((PassWalk) newPass).getPerson().getName(),
                    ((PassWalk) existPass).getPerson().getName());
        }
        return false; // Обработка других типов объектов Pass может быть добавлена здесь
    }

//    void checkOverlapTime(Pass newPass) {
//        List<Pass> passesByUser = repository.findPassesByUserIdOrderByAddedAtDesc(newPass.getUser().getId());
//        List<Pass> filteredPassesByUser = passesByUser.stream()
//                .filter(pass -> pass.getClass().equals(newPass.getClass()))
//                .toList();
//
//        Optional<Pass> overlapPass = filteredPassesByUser.stream()
//                //чтобы сравнить car или person в зависимости от типа пропуска, необходимо привести сравниваемые
//                //пропуска к соответствующему типу (т.к. в PassWalk нет поля Car и метода getCar, и наоборот)
//                .filter(existPass -> {
////                    if (newPass.getClass() != existPass.getClass()) {
////                        throw new IllegalArgumentException("Types of newPass and existPass should be the same");
////                    }
//                    if (existPass instanceof PassAuto) {
//                        PassAuto newPassAuto = (PassAuto) newPass;
//                        PassAuto existPassAuto = (PassAuto) existPass;
//                        return Objects.equals(newPassAuto.getCar().getLicensePlate(), existPassAuto.getCar().getLicensePlate());
//                    } else if (existPass instanceof PassWalk) {
//                         PassWalk newPassWalk = (PassWalk) newPass;
//                        PassWalk existPassWalk = (PassWalk) existPass;
//                        try {
//                            return Objects.equals(newPassWalk.getPerson().getName(), existPassWalk.getPerson().getName());
//                        } catch (NullPointerException e) {
//                            log.warn(e.getMessage(), e);
//                        }
//                        return false;
//                    } else {
//                        return false; // Обработка других типов объектов Pass
//                    }
//                })
//
//                .filter(existPass -> Objects.equals(existPass.getStatus(), PassStatus.ACTIVE))
//                .filter(existPass -> !Objects.equals(newPass.getId(), existPass.getId()))
//                .filter(existPass -> Objects.equals(newPass.getTerritory(), existPass.getTerritory()))
//                // время пропусков пересекается при выполнении двух условий:
//                //- время окончания нового пропуска больше времени начала существующего пропуска
//                //- время начала нового пропуска меньше времени окончания существующего пропуска
//                .filter(existPass -> newPass.getEndTime().isAfter(existPass.getStartTime()) &&
//                        newPass.getStartTime().isBefore(existPass.getEndTime()))
//                .findFirst();
//
//        if (overlapPass.isPresent()) {
//            String message = String.format("Reject operation: user [%s] already has such a pass with " +
//                    "overlapping time [%s]", newPass.getUser().getId(), overlapPass.get().getId());
//            log.debug(message);
//            throw new IllegalArgumentException(message);
//        }
//    }


    /**
     * Каждую минуту ищет все активные пропуска с истекшим временем действия,
     * затем по каждому найденному пропуску ищет зафиксированные пересечения.
     * Если пересечений не было, присваивает пропуску статус "устаревший" (PassStatus.OUTDATED).
     * Если пересечения были, и последнее было на выезд - статус "выполнен" (PassStatus.COMPLETED).
     * Если пересечения были, и последнее было на въезд - статус "предупреждение" (PassStatus.WARNING).
     * После этого сохраняет пропуск и вызывает метод оповещения фронтенда об изменениях (пока нет :).
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkPassesOnEndTimeReached() {
        if (LocalDateTime.now().getHour() != hourForLogInScheduledCheck) {
            hourForLogInScheduledCheck = LocalDateTime.now().getHour();
            log.debug("Scheduled method 'checkPassesOnEndTimeReached' continues to work");
        }
      
        List<Pass> passes = repository.findByEndTimeIsBeforeAndStatusLike(LocalDateTime.now(), PassStatus.ACTIVE);
        if (passes.isEmpty()) {return;}

        log.info("Method {}, endTime reached on {} active pass(es)", MethodLog.getMethodName(), passes.size());

        for (Pass pass : passes) {
            List<Crossing> passCrossings = crossingRepository.findCrossingsByPassId(pass.getId());

            if (passCrossings.isEmpty()) {
                pass.setStatus(PassStatus.OUTDATED);
            } else {
                Crossing lastCrossing = passCrossings.stream()
                        .max(Comparator.comparing(Crossing::getLocalDateTime))
                        .orElse(null);
                if (lastCrossing.getDirection().equals(Direction.OUT)) {
                    pass.setStatus(PassStatus.COMPLETED);
                } else if (lastCrossing.getDirection().equals(Direction.IN)) {
                    pass.setStatus(PassStatus.WARNING);
                }
            }
            repository.save(pass);

            log.info("Pass [UUID - {}], exist {} crossings, changed status on {}",
                    pass.getId(),passes.size(), pass.getStatus());
            //отправка сообщения на фронт о том, что данные пропусков изменены?
        }
    }
}
