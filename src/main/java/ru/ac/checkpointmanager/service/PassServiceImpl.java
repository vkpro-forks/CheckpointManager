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
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.enums.PassStatus;
import ru.ac.checkpointmanager.repository.CrossingRepository;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import static ru.ac.checkpointmanager.utils.Mapper.*;

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
        checkCrossCovering(pass);

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
        checkCrossCovering(pass);

        logger.info(String.format("Invoked method %s [%s]", MethodLog.getMethodName(), pass.getId()));
        Pass foundPass = findPass(pass.getId());

        if (!foundPass.getStatus().equals(PassStatus.ACTIVE)) {
            throw new IllegalStateException("This pass is not active, it cannot be changed. " +
                    "You can only change the active pass");
        }

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
        Territory territory = newPass.getTerritory();

        List<Territory> territories = toTerritories(userService.findTerritoriesByUserId(newPass.getUser().getId()));
        if (!territories.contains(territory)) {
            throw new TerritoryNotFoundException("The user does not have authority for this territory");
        }
    }

    /**
     * @param newPass добавляемый или изменяемый пропуск
     * @exception IllegalArgumentException, если в системе существует активный пропуск,
     * созданный тем же юзером, в котором совпадает территория, данные машины/человека
     * и пересекается время действия
     */
    void checkCrossCovering(Pass newPass) {
        List<Pass> passes = repository.findPassesByUserIdOrderByAddedAtDesc(newPass.getUser().getId());

        for (Pass existPass : passes) {

            if (existPass.getStatus().equals(PassStatus.ACTIVE) &&
                newPass.getTerritory().equals(existPass.getTerritory()) /*&&
                Objects.equals(newPass.getCar, existPass.getCar) &&
                Objects.equals(newPass.getPerson, existPass.getPerson)*/) {

                if (newPass.getEndTime().isAfter(existPass.getStartTime()) &&
                    newPass.getStartTime().isBefore(existPass.getEndTime())) {

                    throw new IllegalArgumentException("This user already has such pass with crosscovered time");
                }
            }
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
