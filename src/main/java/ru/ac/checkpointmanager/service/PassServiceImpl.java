package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.PassNotFoundException;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.Pass;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.enums.PassStatus;
import ru.ac.checkpointmanager.repository.CrossingRepository;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service class for Pass domain objects
 *
 * @author Dmitry Ldv236
 */
@Service
@RequiredArgsConstructor
public class PassServiceImpl implements PassService{
    private final Logger logger = LoggerFactory.getLogger(PassServiceImpl.class);
    private final PassRepository repository;
    private final CrossingRepository crossingRepository;
    private final TerritoryService territoryService;
    private final UserService userService;

    @Override
    public Pass addPass(Pass pass) {
        checkUserTerritoryRelation(pass);
        checkOverlapTime(pass);

        logger.info(String.format("Invoked method %s", MethodLog.getMethodName()));
        pass.setStatus(PassStatus.ACTIVE);
        return repository.save(pass);
    }

    @Override
    public List<Pass> findPasses() {
        logger.info(String.format("Invoked method %s", MethodLog.getMethodName()));
        return repository.findAll();
    }

    @Override
    public Pass findPass(UUID id) {
        logger.info(String.format("Invoked method %s [%s]", MethodLog.getMethodName(), id));
        return repository.findById(id).orElseThrow(
                () -> new PassNotFoundException(String.format("Pass not found [userId=%s]", id)));
    }

    @Override
    public List<Pass> findPassesByUser(UUID userId) {
        logger.info(String.format("Invoked method %s [%s]", MethodLog.getMethodName(), userId));
        List<Pass> foundPasses = repository.findPassesByUserIdOrderByAddedAtDesc(userId);

        if (foundPasses.isEmpty()) {
            throw new PassNotFoundException(String.format("For User [id=%s] not exist any Passes", userId));
        }
        return foundPasses;
    }

    @Override
    public List<Pass> findPassesByTerritory(UUID terId) {
        logger.info(String.format("Invoked method %s [%s]", MethodLog.getMethodName(), terId));
        List<Pass> foundPasses = repository.findPassesByTerritoryIdOrderByAddedAtDesc(terId);

        if (foundPasses.isEmpty()) {
            throw new PassNotFoundException(String.format("For Territory [id=%s] not exist any Passes", terId));
        }
        return foundPasses;
    }

    @Override
    public Pass updatePass(Pass pass) {
        checkUserTerritoryRelation(pass);
        checkOverlapTime(pass);

        logger.info(String.format("Invoked method %s [%s]", MethodLog.getMethodName(), pass.getId()));
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
    public Pass cancelPass(UUID id) {
        logger.info(String.format("Invoked method %s [%s]", MethodLog.getMethodName(), id));
        Pass pass = findPass(id);

        if (!pass.getStatus().equals(PassStatus.ACTIVE)) {
            throw new IllegalStateException("You can only cancel an active Pass");
        }

        repository.cancelById(id);
        pass.setStatus(PassStatus.CANCELLED);
        return pass;
    }

    @Override
    public Pass activateCancelledPass(UUID id) {
        logger.info(String.format("Invoked method %s [%s]", MethodLog.getMethodName(), id));
        Pass pass = findPass(id);

        if (!pass.getStatus().equals(PassStatus.CANCELLED)) {
            throw new IllegalStateException("You can only activate a previously cancelled pass");
        }

        if (pass.getEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("This pass has already expired");
        }

        repository.activateById(id);
        pass.setStatus(PassStatus.ACTIVE);
        return pass;
    }

    @Override
    public Pass unWarningPass(UUID id) {
        logger.info(String.format("Invoked method %s [%s]", MethodLog.getMethodName(), id));
        Pass pass = findPass(id);

        if (!pass.getStatus().equals(PassStatus.WARNING)) {
            throw new IllegalStateException("You can only to unwarnining a previously warninged pass");
        }

        repository.completedStatusById(id);
        pass.setStatus(PassStatus.COMPLETED);
        return pass;
    }


    @Override
    public void deletePass(UUID id) {
        logger.info(String.format("Invoked method %s [%s]", MethodLog.getMethodName(), id));
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
        //два способа - через лист территорий, найденных по userId,
        // и проверка соответствия id в связующей таблице sql-запросом
        //возможна разница в производительности, пока отставлю оба
//        Territory territory = newPass.getTerritory();
//
//        List<Territory> territories = toTerritories(userService.findTerritoriesByUserId(newPass.getUser().getId()));
//        if (!territories.contains(territory)) {
//            throw new TerritoryNotFoundException("The user does not have authority for this territory");
//        }
        UUID userId = newPass.getUser().getId();
        UUID territoryId = newPass.getTerritory().getId();
        if (!repository.checkUserTerritoryRelation(userId, territoryId)) {
            String message = String.format("Reject operation: user [%s] not have permission to create passes " +
                    "for this territory [%s]", userId, territoryId);
            logger.warn(message);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * @param newPass добавляемый или изменяемый пропуск
     * @exception IllegalArgumentException, если в системе существует другой активный пропуск,
     * созданный тем же юзером, в котором совпадает территория, данные машины/человека
     * и пересекается (накладывается) время действия
     */
    void checkOverlapTime(Pass newPass) {
        List<Pass> passesByUser = repository.findPassesByUserIdOrderByAddedAtDesc(newPass.getUser().getId());

        Optional<Pass> overlapPass = passesByUser.stream()
                .filter(existPass -> existPass.getStatus().equals(PassStatus.ACTIVE))
                .filter(existPass -> !newPass.getId().equals(existPass.getId()))
                .filter(existPass -> newPass.getTerritory().equals(existPass.getTerritory()))
                //раскомментить когда в Pass будут добавлены Car и Person
//                .filter(existPass -> Objects.equals(newPass.getCar, existPass.getCar))
//                .filter(existPass -> Objects.equals(newPass.getPerson, existPass.getPerson))
                .filter(existPass -> newPass.getEndTime().isAfter(existPass.getStartTime()) &&
                        newPass.getStartTime().isBefore(existPass.getEndTime()))
                .findFirst();

        if (overlapPass.isPresent()) {
            String message = String.format("Reject operation: user [%s] already has such a pass with " +
                            "overlapping time [%s]", newPass.getUser().getId(), overlapPass.get().getId());
            logger.info(message);
            throw new IllegalArgumentException(message);
        }
    }
    /**
     * С заданной периодичностью ищет все активные пропуска с истекшим временем действия,
     * затем по каждому наденному пропуску ищет зафиксированные пересечения.
     * Если пересечений не было, присваивает пропуску статус "устаревший" (PassStatus.OUTDATED).
     * Если пересечения были, и последнее было на выезд - статус "выполнен" (PassStatus.COMPLETED).
     * Если пересечения были, и последнее было на въезд - статус "предупреждение" (PassStatus.WARNING).
     * После этого сохраняет пропуск и вызывает метод оповещения фронтенда об изменениях (пока нет :).
     */
//    @Scheduled(cron = "0 0/1 * * * *")
    @Scheduled(fixedDelay = 5_000L)
    public void checkPassesOnEndTimeReached() {
        List<Pass> passes = repository.findByEndTimeIsBeforeAndStatusLike(
                LocalDateTime.now(), PassStatus.ACTIVE);
        if (passes.isEmpty()) {return;}

        logger.info(String.format("Invoked method %s, endTime reached on %d active pass(es)"
                , MethodLog.getMethodName()
                , passes.size()));

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

            logger.info(String.format("For Pass [id %s] changed status on %s"
                    , pass.getId()
                    , pass.getStatus()));
            //вот здесь
        }
        //вызов метода отправки сообщения на фронт о том, что данные пропусков изменены
        //вопрос - на какой фронт? Не всем же клиентам, а только тем, к чьим территориям относились пропуска!
        //тогда этот вызов должен быть сразу после сохранения пропуска, т.е. отправка по каждому измененному пропуску
    }
}
