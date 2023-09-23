package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.model.Pass;

import java.util.List;
import java.util.UUID;

public interface PassService {

    Pass addPass(Pass pass);

    List<Pass> findPasses();

    Pass findPass(UUID id);

    List<Pass> findPassesByUser(UUID userId);

    List<Pass> findPassesByTerritory(UUID terId);

    Pass updatePass(Pass pass);

    Pass cancelPass(UUID id);

    Pass activateCancelledPass(UUID id);

    void deletePass(UUID id);

}
