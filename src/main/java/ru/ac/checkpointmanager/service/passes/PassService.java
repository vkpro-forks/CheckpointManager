package ru.ac.checkpointmanager.service.passes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.ac.checkpointmanager.model.passes.Pass;

import java.util.UUID;

public interface PassService {

    Pass addPass(Pass pass);

    Page<Pass> findPasses(Pageable pageable);

    Pass findPass(UUID id);

    Page<Pass> findPassesByUser(UUID userId, Pageable pageable);

    Page<Pass> findPassesByTerritory(UUID terId, Pageable pageable);

    Pass updatePass(Pass pass);

    Pass cancelPass(UUID id);

    Pass activateCancelledPass(UUID id);

    Pass unWarningPass(UUID id);

    void markFavorite(UUID id);

    void unmarkFavorite(UUID id);

    void deletePass(UUID id);

}
