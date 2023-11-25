package ru.ac.checkpointmanager.service.passes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.passes.PagingParams;
import ru.ac.checkpointmanager.exception.PassNotFoundException;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.repository.CrossingRepository;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.ac.checkpointmanager.utils.StringTrimmer.trimThemAll;

/**
 * Service class for Pass domain objects
 *
 * @author Dmitry Ldv236
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PassServiceImpl implements PassService {

    public static final String PASS_NOT_FOUND_LOG = "[Pass with id: {}] not found";
    public static final String PASS_NOT_FOUND_MSG = "Pass with id: %s not found";
    private final PassRepository repository;
    private final CrossingRepository crossingRepository;
    private final UserRepository userRepository;
    private final TerritoryRepository territoryRepository;

    private int hourForLogInScheduledCheck;

    @Override
    public Pass addPass(Pass pass) {
        log.info("Method {} [{}]", MethodLog.getMethodName(), pass);

        if (userRepository.findById(pass.getUser().getId()).isEmpty()) {
            throw new UserNotFoundException(String.format("User not found [id=%s]", pass.getUser().getId()));
        }
        if (territoryRepository.findById(pass.getTerritory().getId()).isEmpty()) {
            throw new TerritoryNotFoundException(String.format("Territory not found [id=%s]", pass.getTerritory().getId()));
        }

        checkPassTime(pass);
        checkUserTerritoryRelation(pass);
        checkOverlapTime(pass);

        trimThemAll(pass);

        if (pass.getStartTime().isBefore(LocalDateTime.now())) {
            pass.setStatus(PassStatus.ACTIVE);
        } else {
            pass.setStatus(PassStatus.DELAYED);
        }

        if (pass.getComment() == null || pass.getComment().isBlank()) {
            pass.setComment("Пропуск-" + pass.getId().toString().substring(32));
        }

        Pass savedPass = repository.save(pass);
        log.info("Pass saved [{}]", savedPass);

        return savedPass;
    }

    @Override
    public Page<Pass> findPasses(PagingParams pagingParams) {
        log.debug("Method {}", MethodLog.getMethodName());

        Pageable pageable = PageRequest.of(pagingParams.getPage(), pagingParams.getSize());
        Page<Pass> foundPasses = repository.findAll(pageable);
        if (!foundPasses.hasContent()) {
            throw new PassNotFoundException(String.format(
                    "Page %d (size - %d) does not contain passes, total pages - %d, total elements - %d",
                    pageable.getPageNumber(), pageable.getPageSize(),
                    foundPasses.getTotalPages(), foundPasses.getTotalElements()));
        }

        return foundPasses;
    }

    @Override
    public Pass findPass(UUID id) {
        log.debug("Method {} [UUID - {}]", MethodLog.getMethodName(), id);
        return repository.findById(id).orElseThrow(
                () -> {
                    log.warn(PASS_NOT_FOUND_LOG, id);
                    return new PassNotFoundException(PASS_NOT_FOUND_MSG.formatted(id));
                });
    }

    @Override
    public Page<Pass> findPassesByUser(UUID userId, PagingParams pagingParams) {
        log.debug("Method {} [UUID - {}]", MethodLog.getMethodName(), userId);
        Pageable pageable = PageRequest.of(pagingParams.getPage(), pagingParams.getSize());
        if (userRepository.findById(userId).isEmpty()) {
            throw new UserNotFoundException(String.format("User not found [id=%s]", userId));
        }

        Page<Pass> foundPasses = repository.findPassesByUserId(userId, pageable);
        if (!foundPasses.hasContent()) {
            throw new PassNotFoundException(String.format(
                    "Page %d (size - %d) does not contain passes, total pages - %d, total elements - %d  [user id %s]",
                    pageable.getPageNumber(), pageable.getPageSize(),
                    foundPasses.getTotalPages(), foundPasses.getTotalElements(), userId));
        }

        return foundPasses;
    }

    @Override
    public Page<Pass> findPassesByTerritory(UUID terId, PagingParams pagingParams) {
        log.debug("Method {} [UUID - {}]", MethodLog.getMethodName(), terId);
        Pageable pageable = PageRequest.of(pagingParams.getPage(), pagingParams.getSize());
        if (territoryRepository.findById(terId).isEmpty()) {
            throw new TerritoryNotFoundException(String.format("Territory not found [id=%s]", terId));
        }

        Page<Pass> foundPasses = repository.findPassesByTerritoryId(terId, pageable);

        if (!foundPasses.hasContent()) {
            throw new PassNotFoundException(String.format(
                "Page %d (size - %d) does not contain passes, total pages - %d, total elements - %d  [territory id %s]",
                pageable.getPageNumber(), pageable.getPageSize(),
                foundPasses.getTotalPages(), foundPasses.getTotalElements(), terId));
        }
        return foundPasses;
    }

    @Override
    public Pass updatePass(Pass pass) {
        log.info("Method {} [UUID - {}]", MethodLog.getMethodName(), pass.getId());
        Pass foundPass = findPass(pass.getId());
        if (!foundPass.getStatus().equals(PassStatus.ACTIVE) && !foundPass.getStatus().equals(PassStatus.DELAYED)) {
            throw new IllegalStateException("This pass is not active or delayed, it cannot be changed");
        }
        checkPassTime(pass);
        checkUserTerritoryRelation(pass);
        checkOverlapTime(pass);
        trimThemAll(pass);

        foundPass.setComment(pass.getComment());
        foundPass.setTypeTime(pass.getTypeTime());
        foundPass.setStartTime(pass.getStartTime());
        foundPass.setEndTime(pass.getEndTime());
        foundPass.setAttachedEntity(pass);

        Pass updatedPass = repository.save(foundPass);
        log.info("Pass updated, {}", updatedPass);

        return updatedPass;
    }

    @Override
    @Transactional
    public Pass cancelPass(UUID id) {
        log.info("Method {} [UUID - {}]", MethodLog.getMethodName(), id);

        Pass pass = findPass(id);
        if (!pass.getStatus().equals(PassStatus.ACTIVE) && !pass.getStatus().equals(PassStatus.DELAYED)) {
            throw new IllegalStateException("You can only cancel an active or delayed pass");
        }

        List<Crossing> passCrossings = crossingRepository.findCrossingsByPassId(pass.getId());
        if (passCrossings.isEmpty()) {
            pass.setStatus(PassStatus.CANCELLED);
        } else {
            pass.setStatus(
                    changeStatusForPassWithCrossings(passCrossings));
        }

        Pass cancelledPass = repository.save(pass);
        log.info("Pass [UUID - {}], exist {} crossings, changed status on {}",
                pass.getId(), passCrossings.size(), pass.getStatus());

        return cancelledPass;
    }

    @Override
    public Pass activateCancelledPass(UUID id) {
        log.info("Method {} [UUID - {}]", MethodLog.getMethodName(), id);
        Pass pass = findPass(id);

        if (!pass.getStatus().equals(PassStatus.CANCELLED)) {
            throw new IllegalStateException("You can only activate a previously cancelled pass");
        }

        if (pass.getEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("This pass has already expired");
        }

        if (pass.getStartTime().isBefore(LocalDateTime.now())) {
            pass.setStatus(PassStatus.ACTIVE);
        } else {
            pass.setStatus(PassStatus.DELAYED);
        }

        Pass activatedPass = repository.save(pass);
        log.info("Pass [UUID - {}], changed status on {}", pass.getId(), pass.getStatus());

        return activatedPass;
    }

    @Override
    public Pass unWarningPass(UUID id) {
        log.info("Method {} [UUID - {}]", MethodLog.getMethodName(), id);

        Pass pass = findPass(id);
        if (!pass.getStatus().equals(PassStatus.WARNING)) {
            throw new IllegalStateException("You can only to unwarnining a previously warninged pass");
        }
        pass.setStatus(PassStatus.COMPLETED);

        Pass completedPass = repository.save(pass);
        log.info("Pass [UUID - {}], changed status on {}", pass.getId(), pass.getStatus());

        return completedPass;
    }

    @Override
    public void markFavorite(UUID id) {
        log.info("Method {} [UUID - {}]", MethodLog.getMethodName(), id);
        Pass pass = findPass(id);
        pass.setFavorite(true);
        repository.save(pass);
        log.info("Pass [UUID - {}] marked favorite", id);
    }

    @Override
    public void unmarkFavorite(UUID id) {
        log.info("Method {} [UUID - {}]", MethodLog.getMethodName(), id);
        Pass pass = findPass(id);
        pass.setFavorite(false);
        repository.save(pass);
        log.info("Pass [UUID - {}] unmarked favorite", id);
    }

    @Override
    public void deletePass(UUID id) {
        log.info("Method {} [UUID - {}]", MethodLog.getMethodName(), id);
        if (repository.findById(id).isEmpty()) {
            log.warn(PASS_NOT_FOUND_LOG, id);
            throw new PassNotFoundException(PASS_NOT_FOUND_MSG.formatted(id));
        }
        repository.deleteById(id);
        log.info("[Pass with id: {}] successfully deleted", id);
    }

    /**
     * Проверяет, что в добавляемом или изменяемом пропуске
     * время окончания больше чем время начала
     *
     * @param newPass добавляемый или изменяемый пропуск
     * @throws IllegalArgumentException "The start time must be earlier than the end time"
     */
    private void checkPassTime(Pass newPass) {
        if (!newPass.getStartTime().isBefore(newPass.getEndTime())) {

            String message = String.format("The start time is after the end time [UUID - %s], start - %s, end - %s",
                    newPass.getId(), newPass.getStartTime(), newPass.getEndTime());
            log.info(message);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * @param newPass добавляемый или изменяемый пропуск
     * @throws TerritoryNotFoundException, если указанный юзер не имеет связи с указанной территорией,
     *                                     т.е. не имеет права создавать пропуска для этой территории
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

    /**
     * @param newPass добавляемый или изменяемый пропуск
     * @throws IllegalArgumentException, если в системе существует другой активный пропуск,
     *                                   созданный тем же юзером, в котором совпадает территория, данные машины/человека
     *                                   и пересекается (накладывается) время действия
     */
    void checkOverlapTime(Pass newPass) {
        List<Pass> passesByUser = repository.findAllPassesByUserId(newPass.getUser().getId());

        Optional<Pass> overlapPass = passesByUser.stream()
                .filter(existPass -> existPass.getClass().equals(newPass.getClass()))
                .filter(existPass -> existPass.getStatus().equals(PassStatus.ACTIVE) ||
                        existPass.getStatus().equals(PassStatus.DELAYED))
                .filter(existPass -> existPass.compareByFields(newPass))
                .findFirst();

        if (overlapPass.isPresent()) {
            String message = String.format("Reject operation: user [UUID - %s] already has such a pass with " +
                    "overlapping time [UUID - %s]", newPass.getUser().getId(), overlapPass.get().getId());
            log.info(message);
            throw new IllegalArgumentException(message);
        }
    }

     * Каждую минуту обновляет статусы активных и отложенных пропусков
     */
    @Scheduled(cron = "0 * * * * ?")
    public void updatePassStatusByScheduler() {
        if (LocalDateTime.now().getHour() != hourForLogInScheduledCheck) {
            hourForLogInScheduledCheck = LocalDateTime.now().getHour();
            log.debug("Method {} continues to work", MethodLog.getMethodName());
        }

        updateDelayedPassesOnStartTimeReached();
        updateActivePassesOnEndTimeReached();
    }

    /**
     * Ищет все отложенные пропуска с начавшимся временем действия,
     * присваивает им статус "активный" (PassStatus.ACTIVE)
     */
    public void updateDelayedPassesOnStartTimeReached() {
        List<Pass> passes = repository.findPassesByStatusAndTimeBefore(PassStatus.DELAYED.toString(),
                "startTime", LocalDateTime.now().plusMinutes(1));
        if (passes.isEmpty()) {
            return;
        }

        log.info("Method {}, startTime reached on {} delayed pass(es)", MethodLog.getMethodName(), passes.size());

        for (Pass pass : passes) {
            pass.setStatus(PassStatus.ACTIVE);
            repository.save(pass);

            log.debug("Pass [UUID - {}], changed status on {}",
                    pass.getId(), pass.getStatus());
        }
    }

    /**
     * Ищет все активные пропуска с истекшим временем действия,
     * затем по каждому найденному пропуску ищет зафиксированные пересечения.
     * Если пересечений не было, присваивает пропуску статус "устаревший" (PassStatus.OUTDATED),
     * в противном случае присваивает статус с помощью метода {@code changeStatusForPassWithCrossings}
     */
    public void updateActivePassesOnEndTimeReached() {
        List<Pass> passes = repository.findPassesByStatusAndTimeBefore(PassStatus.ACTIVE.toString(),
                "endTime", LocalDateTime.now());
        if (passes.isEmpty()) {
            return;
        }

        log.info("Method {}, endTime reached on {} active pass(es)", MethodLog.getMethodName(), passes.size());

        for (Pass pass : passes) {
            List<Crossing> passCrossings = crossingRepository.findCrossingsByPassId(pass.getId());
            if (passCrossings.isEmpty()) {
                pass.setStatus(PassStatus.OUTDATED);
            } else {
                pass.setStatus(
                        changeStatusForPassWithCrossings(passCrossings));
            }
            repository.save(pass);

            log.info("Pass [UUID - {}], exist {} crossings, changed status on {}",
                    pass.getId(), passCrossings.size(), pass.getStatus());
        }
    }

    /**
     * Возвращает статус для отменяемого или истекшего пропуска:
     * Если пересечения были, и последнее было на выезд - статус "выполнен" (PassStatus.COMPLETED).
     * Если пересечения были, и последнее было на въезд - статус "предупреждение" (PassStatus.WARNING).
     *
     * @param crossings список пересечений по проверяемому пропуску
     * @return {@code PassStatus}
     */
    private PassStatus changeStatusForPassWithCrossings(List<Crossing> crossings) {
        Crossing lastCrossing = crossings.stream()
                .max(Comparator.comparing(Crossing::getLocalDateTime))
                .get();

        if (lastCrossing.getDirection().equals(Direction.OUT)) {
            return PassStatus.COMPLETED;
        } else {
            return PassStatus.WARNING;
        }
    }
}
