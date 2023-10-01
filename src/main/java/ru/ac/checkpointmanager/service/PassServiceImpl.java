package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.PassNotFoundException;
import ru.ac.checkpointmanager.model.Pass;
import ru.ac.checkpointmanager.model.enums.PassStatus;
import ru.ac.checkpointmanager.repository.PassRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PassServiceImpl implements PassService{

    private final PassRepository repository;
//    private final CrossingRepository crossingRepository;

    @Override
    public Pass addPass(Pass pass) {

        pass.setAddedAt(LocalDateTime.now());
        pass.setStatus(PassStatus.ACTIVE);
        return repository.save(pass);
    }

    @Override
    public List<Pass> findPasses() {
        return repository.findAll();
    }

    @Override
    public Pass findPass(UUID id) {
        return repository.findById(id).orElseThrow(
                () -> new PassNotFoundException(String.format("Pass not found [userId=%s]", id)));
    }

    @Override
    public List<Pass> findPassesByUser(UUID userId) {

        List<Pass> foundPasses = repository.findPassesByUserIdOrderByAddedAtDesc(userId);

        if (foundPasses.isEmpty()) {
            throw new PassNotFoundException(String.format("For User [id=%s] not exist any Passes", userId));
        }
        return foundPasses;
    }

    @Override
    public List<Pass> findPassesByTerritory(UUID terId) {

        List<Pass> foundPasses = repository.findPassesByTerritoryIdOrderByAddedAtDesc(terId);

        if (foundPasses.isEmpty()) {
            throw new PassNotFoundException(String.format("For Territory [id=%s] not exist any Passes", terId));
        }
        return foundPasses;
    }

    @Override
    public Pass updatePass(Pass pass) {

        Pass foundPass = repository.findById(pass.getId())
                .orElseThrow(() -> new PassNotFoundException(String.format("Pass not found [Id=%s]", pass.getId())));

        foundPass.setTypeTime(pass.getTypeTime());
        foundPass.setTerritory(pass.getTerritory());
        foundPass.setNote(pass.getNote());
        foundPass.setStartTime(pass.getStartTime());
        foundPass.setEndTime(pass.getEndTime());

        return repository.save(foundPass);
    }

    @Override
    public Pass cancelPass(UUID id) {
        Pass pass = repository.findById(id)
                .orElseThrow(() -> new PassNotFoundException(String.format("Pass not found [Id=%s]", id)));

        if (!pass.getStatus().equals(PassStatus.ACTIVE)) {
            throw new IllegalStateException("You can only cancel an active Pass");
        }

        repository.cancelById(id);
        return repository.findById(id).get();
    }

    @Override
    public Pass activateCancelledPass(UUID id) {
        Pass pass = repository.findById(id)
                .orElseThrow(() -> new PassNotFoundException(String.format("Pass not found [Id=%s]", id)));

        if (!pass.getStatus().equals(PassStatus.CANCELLED)) {
            throw new IllegalStateException("You can only activate a previously cancelled pass");
        }

        if (pass.getEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("This pass has already expired");
        }

        repository.activateById(id);
        return repository.findById(id).get();
    }

    @Override
    public void deletePass(UUID id) {
        if (repository.findById(id).isEmpty()) {
            throw new PassNotFoundException(String.format("Pass not found [Id=%s]", id));
        }
        repository.deleteById(id);
    }

//    @Scheduled(cron = "0 0/1 * * * *")
    @Scheduled(fixedDelay = 5_000L)
    public void fetchDatabaseRecords() {
        List<Pass> passes = repository.findByEndTimeIsBeforeAndStatusLike(
                LocalDateTime.now(), PassStatus.ACTIVE);
        if (passes.isEmpty()) {return;}

//        for (Pass pass : passes) {
//            List<Crossing> passCrossings = crossingRepository.findByPassId(pass.getId());
//            if (passCrossings.isEmpty()) {
//                pass.setStatus(PassStatus.OUTDATED);
//            } else {
////                Crossing lastCrossing = Collections.max(passCrossings, Comparator.comparing(Crossing::getEndTime));
//                Crossing lastCrossing = passCrossings.stream()
//                        .max(Comparator.comparing(Crossing::getEndTime));
////                        .orElse(null);
//                if (lastCrossing.getDirection.equals())
//            }
//        }

//        records.forEach(record -> {
//            logger.info("Notification was sent");
//            telegramBot.execute(new SendMessage(record.getChatId(), String.format("Привет! Не забудь:\n%s" +
//                    " , в %s", record.getNotification(), record.getAlarmDate())));
//        });
    }
}
