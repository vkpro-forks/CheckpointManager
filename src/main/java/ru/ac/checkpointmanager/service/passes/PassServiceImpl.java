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
import ru.ac.checkpointmanager.exception.pass.ModifyPassException;
import ru.ac.checkpointmanager.exception.pass.OverlapPassException;
import ru.ac.checkpointmanager.exception.pass.PassNotFoundException;
import ru.ac.checkpointmanager.exception.pass.UserTerritoryRelationException;
import ru.ac.checkpointmanager.mapper.PassMapper;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.projection.PassInOutViewProjection;
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

    private static final String PASS_NOT_FOUND = "Pass [%s] not found";
    private static final String PAGE_NO_CONTENT = "Page %d, size - %d, has no content (total pages - %d, total elements - %d)";
    private static final String PASS_NOT_UPDATE = "Pass [%s] cannot be updated (%s)";
    private static final String PASS_NOT_CANCEL = "Pass [%s] cannot be canceled (%s)";
    private static final String PASS_NOT_ACTIVATED = "Pass [%s] cannot be activated (%s)";
    private static final String METHOD_INVOKE = "Method {} [{}]";
    private static final String PASS_STATUS = "Pass [{}], changed status on {}";
    private static final String PASS_STATUS_CROSS = "Pass [{}], exist {} crossings, changed status on {}";
    private static final String USER_TER_REL = "Reject: user [%s] not have permission to create passes for territory [%s]";
    private static final String OVERLAP_PASS = "Reject: user [%s] has an overlapping pass [%s]";

    private final PassRepository passRepository;
    private final CrossingRepository crossingRepository;
    private final UserService userService;
    private final TerritoryService territoryService;
    private final PassMapper mapper;

    private int hourForLogInScheduledCheck;

    @Override
    @Transactional
    public PassResponseDTO addPass(PassCreateDTO passCreateDTO) {
        log.info(METHOD_INVOKE, MethodLog.getMethodName(), passCreateDTO);
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
        log.debug(METHOD_INVOKE, MethodLog.getMethodName(), "all");

        Pageable pageable = PageRequest.of(pagingParams.getPage(), pagingParams.getSize());
        Page<Pass> foundPasses = passRepository.findAll(pageable);
        if (!foundPasses.hasContent()) {
            log.warn(PAGE_NO_CONTENT.formatted(pageable.getPageNumber(), pageable.getPageSize(),
                    foundPasses.getTotalPages(), foundPasses.getTotalElements()));
        }

        return foundPasses.map(mapper::toPassDTO);
    }

    @Override
    public PassResponseDTO findById(UUID id) {
        log.debug(METHOD_INVOKE, MethodLog.getMethodName(), id);
        return mapper.toPassDTO(findPassById(id));
    }

    @Override
    public Pass findPassById(UUID id) {
        return passRepository.findById(id).orElseThrow(
                () -> {
                    log.warn(PASS_NOT_FOUND.formatted(id));
                    return new PassNotFoundException(PASS_NOT_FOUND.formatted(id));
                });
    }

    @Override
    public Page<PassResponseDTO> findPassesByUser(UUID userId, PagingParams pagingParams) {
        log.debug(METHOD_INVOKE, MethodLog.getMethodName(), userId);
        Pageable pageable = PageRequest.of(pagingParams.getPage(), pagingParams.getSize());
        userService.findById(userId);

        Page<Pass> foundPasses = passRepository.findPassesByUserId(userId, pageable);
        if (!foundPasses.hasContent()) {
            log.warn(PAGE_NO_CONTENT.formatted(pageable.getPageNumber(), pageable.getPageSize(),
                    foundPasses.getTotalPages(), foundPasses.getTotalElements()));
        }

        return foundPasses.map(mapper::toPassDTO);
    }

    @Override
    public Page<PassResponseDTO> findPassesByTerritory(UUID terId, PagingParams pagingParams) {
        log.debug(METHOD_INVOKE, MethodLog.getMethodName(), terId);
        Pageable pageable = PageRequest.of(pagingParams.getPage(), pagingParams.getSize());
        territoryService.findById(terId);
        Page<Pass> foundPasses = passRepository.findPassesByTerritoryId(terId, pageable);
        if (!foundPasses.hasContent()) {
            log.warn(PAGE_NO_CONTENT.formatted(pageable.getPageNumber(), pageable.getPageSize(),
                    foundPasses.getTotalPages(), foundPasses.getTotalElements()));
        }
        return foundPasses.map(mapper::toPassDTO);
    }

    @Override
    public Page<PassInOutViewProjection> findEventsByUser(UUID userId, PagingParams pagingParams) {
        log.debug(METHOD_INVOKE, MethodLog.getMethodName(), userId);
        userService.findById(userId);
        Pageable pageable = PageRequest.of(pagingParams.getPage(), pagingParams.getSize());

        return passRepository.findEventsByUser(userId, pageable);
    }

    @Override
    public Page<PassInOutViewProjection> findEventsByTerritory(UUID terId, PagingParams pagingParams) {
        log.debug(METHOD_INVOKE, MethodLog.getMethodName(), terId);
        territoryService.findById(terId);
        Pageable pageable = PageRequest.of(pagingParams.getPage(), pagingParams.getSize());

        return passRepository.findEventsByTerritory(terId, pageable);
    }

    @Override
    public PassResponseDTO updatePass(PassUpdateDTO passUpdateDTO) {
        log.debug(METHOD_INVOKE, MethodLog.getMethodName(), passUpdateDTO);

        UUID passId = passUpdateDTO.getId();
        Pass existPass = findPassById(passId);
        PassStatus passStatus = existPass.getStatus();
        if (passStatus != PassStatus.ACTIVE && passStatus != PassStatus.DELAYED) {
            log.warn(PASS_NOT_UPDATE.formatted(passId, passStatus));
            throw new ModifyPassException(PASS_NOT_UPDATE.formatted(passId, passStatus));
        }

        User user = userService.findByPassId(passId);
        Territory territory = territoryService.findByPassId(passId);
        checkUserTerritoryRelation(user, territory);
        Pass newStatePass = mapper.toPass(passUpdateDTO, user, territory);

        checkOverlapTime(newStatePass);
        trimThemAll(newStatePass);

        existPass.setComment(newStatePass.getComment());
        existPass.setTypeTime(newStatePass.getTypeTime());
        existPass.setStartTime(newStatePass.getStartTime());
        existPass.setEndTime(newStatePass.getEndTime());
        existPass.setAttachedEntity(newStatePass);

        Pass updatedPass = passRepository.save(existPass);
        log.info("Pass updated: {}", updatedPass);

        return mapper.toPassDTO(updatedPass);
    }

    @Override
    @Transactional
    public PassResponseDTO cancelPass(UUID id) {
        log.info(METHOD_INVOKE, MethodLog.getMethodName(), id);

        Pass pass = findPassById(id);
        PassStatus passStatus = pass.getStatus();
        if (passStatus != PassStatus.ACTIVE && passStatus != PassStatus.DELAYED) {
            log.warn(PASS_NOT_CANCEL.formatted(id, passStatus));
            throw new ModifyPassException(PASS_NOT_CANCEL.formatted(id, passStatus));
        }

        List<Crossing> passCrossings = crossingRepository.findCrossingsByPassId(pass.getId());
        PassStatus targetStatus;
        if (passCrossings.isEmpty()) {
            targetStatus = PassStatus.CANCELLED;
        } else {
            targetStatus = switch (pass.getExpectedDirection()) {
                case IN -> PassStatus.COMPLETED;
                case OUT -> PassStatus.WARNING;
            };
        }
        pass.setStatus(targetStatus);

        pass = passRepository.save(pass);
        log.info(PASS_STATUS_CROSS, id, passCrossings.size(), targetStatus);
        return mapper.toPassDTO(pass);
    }

    @Override
    public PassResponseDTO activateCancelledPass(UUID id) {
        log.info(METHOD_INVOKE, MethodLog.getMethodName(), id);

        Pass pass = findPassById(id);
        PassStatus passStatus = pass.getStatus();
        if (passStatus != PassStatus.CANCELLED) {
            log.warn(PASS_NOT_ACTIVATED.formatted(id, passStatus));
            throw new ModifyPassException(PASS_NOT_ACTIVATED.formatted(id, passStatus));
        }

        if (pass.getEndTime().isBefore(LocalDateTime.now())) {
            log.warn(PASS_NOT_ACTIVATED.formatted(id, "it's expired"));
            throw new ModifyPassException(PASS_NOT_ACTIVATED.formatted(id, "it's expired"));
        }

        if (pass.getStartTime().isBefore(LocalDateTime.now())) {
            pass.setStatus(PassStatus.ACTIVE);
        } else {
            pass.setStatus(PassStatus.DELAYED);
        }
        Pass activatedPass = passRepository.save(pass);
        log.info(PASS_STATUS, pass.getId(), pass.getStatus());

        return mapper.toPassDTO(activatedPass);
    }

    @Override
    public PassResponseDTO unWarningPass(UUID id) {
        log.info(METHOD_INVOKE, MethodLog.getMethodName(), id);

        Pass pass = findPassById(id);
        if (pass.getStatus() != PassStatus.WARNING) {
            throw new ModifyPassException("You can only to unwarnining a previously warninged pass");
        }

        pass.setStatus(PassStatus.COMPLETED);
        Pass completedPass = passRepository.save(pass);
        log.info(PASS_STATUS, pass.getId(), pass.getStatus());

        return mapper.toPassDTO(completedPass);
    }

    @Override
    public void markFavorite(UUID id) {
        log.info(METHOD_INVOKE, MethodLog.getMethodName(), id);
        Pass pass = findPassById(id);
        pass.setFavorite(true);
        passRepository.save(pass);
        log.info("Pass [{}] marked favorite", id);
    }

    @Override
    public void unmarkFavorite(UUID id) {
        log.info(METHOD_INVOKE, MethodLog.getMethodName(), id);
        Pass pass = findPassById(id);
        pass.setFavorite(false);
        passRepository.save(pass);
        log.info("Pass [{}] unmarked favorite", id);
    }

    @Override
    public void deletePass(UUID id) {
        log.info(METHOD_INVOKE, MethodLog.getMethodName(), id);
        findPassById(id);
        passRepository.deleteById(id);
        log.info("Pass [{}] successfully deleted", id);
    }

    /**
     * Проверяет связь пользователя и территории,
     * которая означает право пользователя создавать пропуска на указанную территорию
     *
     * @param user      пользователь
     * @param territory территория
     */
    private void checkUserTerritoryRelation(User user, Territory territory) {
        UUID userId = user.getId();
        UUID territoryId = territory.getId();

        if (!passRepository.checkUserTerritoryRelation(userId, territoryId)) {
            log.warn(USER_TER_REL.formatted(userId, territoryId));
            throw new UserTerritoryRelationException(USER_TER_REL.formatted(userId, territoryId));
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
            log.info(OVERLAP_PASS.formatted(newPass.getUser().getId(), overlapPass.get().getId()));
            throw new OverlapPassException(OVERLAP_PASS.formatted(newPass.getUser().getId(), overlapPass.get().getId()));
        }
    }

    /**
     * Каждую минуту 00 секунд запускает проверку отложенных и активных пропусков с целью актуализации их статусов
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
     * (условие - время начала меньше текущего времени плюс одна минута -
     * т.к. зафиксированные значения времени не кратны минуте, а содержат также секунды и миллисекунды,
     * то такое условие обеспечивает активацию пропуска чуть заранее - зазор может составить до одной минуты.
     * В противном случае мы бы имели наоборот запаздывание активации длительностью до минуты)
     *
     * @see PassStatus
     */
    public void updateDelayedPassesOnStartTimeReached() {
        PassStatus sourceStatus = PassStatus.DELAYED;
        PassStatus targetStatus = PassStatus.ACTIVE;

        List<Pass> passes = passRepository.findPassesByStatusAndTimeBefore(sourceStatus.toString(),
                "startTime", LocalDateTime.now().plusMinutes(1));
        if (passes.isEmpty()) {
            return;
        }

        log.info("Method {}, startTime reached on {} delayed pass(es)", MethodLog.getMethodName(), passes.size());

        for (Pass pass : passes) {
            pass.setStatus(targetStatus);
            passRepository.save(pass);
            log.debug(PASS_STATUS, pass.getId(), targetStatus);
        }
    }

    /**
     * Обновляет статусы активных пропусков с истекшим временем действия:
     * по каждому активному пропуску ищет зафиксированные пересечения:
     * если пересечений не было, присваивает пропуску статус OUTDATED,
     * в противном случае присваивает статус с помощью метода {@code changeStatusForPassWithCrossings}
     *
     * @see PassStatus
     */
    public void updateActivePassesOnEndTimeReached() {
        PassStatus sourceStatus = PassStatus.ACTIVE;
        PassStatus targetStatus;

        List<Pass> passes = passRepository.findPassesByStatusAndTimeBefore(sourceStatus.toString(),
                "endTime", LocalDateTime.now());
        if (passes.isEmpty()) {
            return;
        }

        log.info("Method {}, endTime reached on {} active pass(es)", MethodLog.getMethodName(), passes.size());

        for (Pass pass : passes) {
            List<Crossing> passCrossings = crossingRepository.findCrossingsByPassId(pass.getId());

            if (passCrossings.isEmpty()) {
                targetStatus = PassStatus.OUTDATED;
            } else {
                targetStatus = switch (pass.getExpectedDirection()) {
                    case IN -> PassStatus.COMPLETED;
                    case OUT -> PassStatus.WARNING;
                };
            }
            pass.setStatus(targetStatus);

            passRepository.save(pass);
            log.info(PASS_STATUS_CROSS, pass.getId(), passCrossings.size(), targetStatus);
        }
    }
}
