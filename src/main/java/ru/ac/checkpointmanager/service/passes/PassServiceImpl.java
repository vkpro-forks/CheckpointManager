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
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassResponseDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.exception.PassNotFoundException;
import ru.ac.checkpointmanager.mapper.PassMapper;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.repository.CrossingRepository;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.service.territories.TerritoryService;
import ru.ac.checkpointmanager.service.user.UserService;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.time.LocalDateTime;
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

    private static final String PASS_NOT_FOUND_LOG = "[Pass with id: {}] not found";
    private static final String PASS_NOT_FOUND_MSG = "Pass with id: %s not found";
    private static final String METHOD_UUID = "Method {} [{}]";
    private static final String PASS_STATUS_CHANGED_LOG = "Pass [UUID - {}], changed status on {}";


    private final PassRepository passRepository;
    private final CrossingRepository crossingRepository;
    private final UserService userService;
    private final TerritoryService territoryService;
    private final PassMapper mapper;

    private int hourForLogInScheduledCheck;

    @Override
    public PassResponseDTO addPass(PassCreateDTO passCreateDTO) {
        log.info("Method {} [{}]", MethodLog.getMethodName(), passCreateDTO);

        UUID userId = passCreateDTO.getUserId();
        UUID territoryId = passCreateDTO.getTerritoryId();
        User user = userService.findUserById(userId);
        Territory territory = territoryService.findTerritoryById(territoryId);
        checkUserTerritoryRelation(user, territory);

        Pass pass = mapper.toPass(passCreateDTO);
        checkOverlapTime(pass);

        trimThemAll(pass);

        if (pass.getStartTime().isBefore(LocalDateTime.now())) {
            pass.setStatus(PassStatus.ACTIVE);
        } else {
            pass.setStatus(PassStatus.DELAYED);
        }

        pass.setId(UUID.randomUUID());
        if (pass.getComment() == null || pass.getComment().isBlank()) {
            pass.setComment("Пропуск-" + pass.getId().toString().substring(32));
        }

        Pass savedPass = passRepository.save(pass);
        log.info("Pass saved [{}]", savedPass);

        return mapper.toPassDTO(savedPass);
    }

    @Override
    public Page<PassResponseDTO> findPasses(PagingParams pagingParams) {
        log.debug("Method {}", MethodLog.getMethodName());

        Pageable pageable = PageRequest.of(pagingParams.getPage(), pagingParams.getSize());
        Page<Pass> foundPasses = passRepository.findAll(pageable);
        if (!foundPasses.hasContent()) {
            throw new PassNotFoundException(String.format(
                    "Page %d (size - %d) does not contain passes, total pages - %d, total elements - %d",
                    pageable.getPageNumber(), pageable.getPageSize(),
                    foundPasses.getTotalPages(), foundPasses.getTotalElements()));
        }

        return foundPasses.map(mapper::toPassDTO);
    }

    @Override
    public PassResponseDTO findById(UUID id) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), id);
        return mapper.toPassDTO(findPassById(id));
    }

    @Override
    public Pass findPassById(UUID id) {
        return passRepository.findById(id).orElseThrow(
                () -> {
                    log.warn(PASS_NOT_FOUND_LOG, id);
                    return new PassNotFoundException(PASS_NOT_FOUND_MSG.formatted(id));
                });
    }

    @Override
    public Page<PassResponseDTO> findPassesByUser(UUID userId, PagingParams pagingParams) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), userId);
        Pageable pageable = PageRequest.of(pagingParams.getPage(), pagingParams.getSize());
        userService.findById(userId);

        Page<Pass> foundPasses = passRepository.findPassesByUserId(userId, pageable);
        if (!foundPasses.hasContent()) {
            throw new PassNotFoundException(String.format(
                    "Page %d (size - %d) does not contain passes, total pages - %d, total elements - %d  [user id %s]",
                    pageable.getPageNumber(), pageable.getPageSize(),
                    foundPasses.getTotalPages(), foundPasses.getTotalElements(), userId));
        }

        return foundPasses.map(mapper::toPassDTO);
    }

    @Override
    public Page<PassResponseDTO> findPassesByTerritory(UUID terId, PagingParams pagingParams) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), terId);
        Pageable pageable = PageRequest.of(pagingParams.getPage(), pagingParams.getSize());
        territoryService.findById(terId);
        Page<Pass> foundPasses = passRepository.findPassesByTerritoryId(terId, pageable);
        if (!foundPasses.hasContent()) {
            throw new PassNotFoundException(String.format(
                    "Page %d (size - %d) does not contain passes, total pages - %d, total elements - %d  [territory id %s]",
                    pageable.getPageNumber(), pageable.getPageSize(),
                    foundPasses.getTotalPages(), foundPasses.getTotalElements(), terId));
        }
        return foundPasses.map(mapper::toPassDTO);
    }

    @Override
    public PassResponseDTO updatePass(PassUpdateDTO passUpdateDTO) {
        log.debug(METHOD_UUID, MethodLog.getMethodName(), passUpdateDTO);

        UUID passId = passUpdateDTO.getId();
        User user = userService.findByPassId(passId);
        Territory territory = territoryService.findByPassId(passId);
        checkUserTerritoryRelation(user, territory);
        Pass pass = mapper.toPass(passUpdateDTO, user, territory);

        Pass foundPass = findPassById(passId);
        if (foundPass.getStatus() != PassStatus.ACTIVE && foundPass.getStatus() != PassStatus.DELAYED) {
            throw new IllegalStateException("This pass is not active or delayed, it cannot be changed");
        }
        checkOverlapTime(pass);
        trimThemAll(pass);

        foundPass.setComment(pass.getComment());
        foundPass.setTypeTime(pass.getTypeTime());
        foundPass.setStartTime(pass.getStartTime());
        foundPass.setEndTime(pass.getEndTime());
        foundPass.setAttachedEntity(pass);

        Pass updatedPass = passRepository.save(foundPass);
        log.info("Pass updated, {}", updatedPass);

        return mapper.toPassDTO(updatedPass);
    }

    @Override
    @Transactional
    public PassResponseDTO cancelPass(UUID id) {
        log.info(METHOD_UUID, MethodLog.getMethodName(), id);

        Pass pass = findPassById(id);
        if (!pass.getStatus().equals(PassStatus.ACTIVE) && !pass.getStatus().equals(PassStatus.DELAYED)) {
            throw new IllegalStateException("You can only cancel an active or delayed pass");
        }

        List<Crossing> passCrossings = crossingRepository.findCrossingsByPassIdOrderByLocalDateTimeDesc(pass.getId());
        if (passCrossings.isEmpty()) {
            pass.setStatus(PassStatus.CANCELLED);
        } else {
            pass.setStatus(
                    changeStatusForPassWithCrossings(passCrossings));
        }

        Pass cancelledPass = passRepository.save(pass);
        log.info("Pass [UUID - {}], exist {} crossings, changed status on {}",
                pass.getId(), passCrossings.size(), pass.getStatus());

        return mapper.toPassDTO(cancelledPass);
    }

    @Override
    public PassResponseDTO activateCancelledPass(UUID id) {
        log.info(METHOD_UUID, MethodLog.getMethodName(), id);
        Pass pass = findPassById(id);

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

        Pass activatedPass = passRepository.save(pass);
        log.info(PASS_STATUS_CHANGED_LOG, pass.getId(), pass.getStatus());

        return mapper.toPassDTO(activatedPass);
    }

    @Override
    public PassResponseDTO unWarningPass(UUID id) {
        log.info(METHOD_UUID, MethodLog.getMethodName(), id);

        Pass pass = findPassById(id);
        if (!pass.getStatus().equals(PassStatus.WARNING)) {
            throw new IllegalStateException("You can only to unwarnining a previously warninged pass");
        }
        pass.setStatus(PassStatus.COMPLETED);

        Pass completedPass = passRepository.save(pass);
        log.info(PASS_STATUS_CHANGED_LOG, pass.getId(), pass.getStatus());

        return mapper.toPassDTO(completedPass);
    }

    @Override
    public void markFavorite(UUID id) {
        log.info(METHOD_UUID, MethodLog.getMethodName(), id);
        Pass pass = findPassById(id);
        pass.setFavorite(true);
        passRepository.save(pass);
        log.info("Pass [UUID - {}] marked favorite", id);
    }

    @Override
    public void unmarkFavorite(UUID id) {
        log.info(METHOD_UUID, MethodLog.getMethodName(), id);
        Pass pass = findPassById(id);
        pass.setFavorite(false);
        passRepository.save(pass);
        log.info("Pass [UUID - {}] unmarked favorite", id);
    }

    @Override
    public void deletePass(UUID id) {
        log.info(METHOD_UUID, MethodLog.getMethodName(), id);
        if (passRepository.findById(id).isEmpty()) {
            log.warn(PASS_NOT_FOUND_LOG, id);
            throw new PassNotFoundException(PASS_NOT_FOUND_MSG.formatted(id));
        }
        passRepository.deleteById(id);
        log.info("[Pass with id: {}] successfully deleted", id);
    }

    /**
     * Проверяет связь пользователя и территории,
     * которая означает право пользователя создавать пропуска на указанную территорию
     *
     * @param user пользователь
     * @param territory территория
     */
    private void checkUserTerritoryRelation(User user, Territory territory) {
        UUID userId = user.getId();
        UUID territoryId = territory.getId();

        if (!passRepository.checkUserTerritoryRelation(userId, territoryId)) {
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
        List<Pass> passesByUser = passRepository.findAllPassesByUserId(newPass.getUser().getId());

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

    /**
     * Каждую минуту запускает проверку отложенных и активных пропусков с целью актуальзации их статусов
     *
     * @see PassServiceImpl#updateDelayedPassesOnStartTimeReached
     * @see PassServiceImpl#updateActivePassesOnEndTimeReached
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
     * Обновляет статусы отложенных пропусков, время начала которых уже наступило, делая их активными
     * (время начала меньше текущего времени плюс одна минута)
     *
     * @see PassStatus
     */
    public void updateDelayedPassesOnStartTimeReached() {
        List<Pass> passes = passRepository.findPassesByStatusAndTimeBefore(PassStatus.DELAYED.toString(),
                "startTime", LocalDateTime.now().plusMinutes(1));
        if (passes.isEmpty()) {
            return;
        }

        log.info("Method {}, startTime reached on {} delayed pass(es)", MethodLog.getMethodName(), passes.size());

        for (Pass pass : passes) {
            pass.setStatus(PassStatus.ACTIVE);
            passRepository.save(pass);

            log.debug(PASS_STATUS_CHANGED_LOG,
                    pass.getId(), pass.getStatus());
        }
    }

    /**
     * Обновляет статусы активных пропусковс истекшим временем действия:
     * по каждому активному пропуску ищет зафиксированные пересечения,
     * если пересечений не было, присваивает пропуску статус OUTDATED,
     * в противном случае присваивает статус с помощью метода {@code changeStatusForPassWithCrossings}
     *
     * @see PassStatus
     */
    public void updateActivePassesOnEndTimeReached() {
        List<Pass> passes = passRepository.findPassesByStatusAndTimeBefore(PassStatus.ACTIVE.toString(),
                "endTime", LocalDateTime.now());
        if (passes.isEmpty()) {
            return;
        }

        log.info("Method {}, endTime reached on {} active pass(es)", MethodLog.getMethodName(), passes.size());

        for (Pass pass : passes) {
            List<Crossing> passCrossings = crossingRepository.findCrossingsByPassIdOrderByLocalDateTimeDesc(pass.getId());
            if (passCrossings.isEmpty()) {
                pass.setStatus(PassStatus.OUTDATED);
            } else {
                pass.setStatus(
                        changeStatusForPassWithCrossings(passCrossings));
            }
            passRepository.save(pass);

            log.info("Pass [UUID - {}], exist {} crossings, changed status on {}",
                    pass.getId(), passCrossings.size(), pass.getStatus());
        }
    }

    /**
     * Возвращает статус для отменяемого или истекшего пропуска:
     * Если пересечения были, и последнее было на выезд - статус COMPLETED.
     * Если пересечения были, и последнее было на въезд - статус WARNING.
     *
     * @param crossings список пересечений по проверяемому пропуску
     * @return {@code PassStatus}
     * @see PassStatus
     */
    private PassStatus changeStatusForPassWithCrossings(List<Crossing> crossings) {

        Crossing lastCrossing = crossings.get(0);
        if (lastCrossing.getDirection().equals(Direction.OUT)) {
            return PassStatus.COMPLETED;
        } else {
            return PassStatus.WARNING;
        }
    }
}
