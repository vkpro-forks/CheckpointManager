package ru.ac.checkpointmanager.service.passes;

import org.springframework.data.domain.Page;
import ru.ac.checkpointmanager.dto.passes.PagingParams;
import ru.ac.checkpointmanager.model.passes.Pass;

import java.util.UUID;

public interface PassService {

    Pass addPass(Pass pass);

//    Page<Pass> findPasses(Pageable pageable);
    Page<Pass> findPasses(PagingParams pagingParams);

    Pass findPass(UUID id);

    Page<Pass> findPassesByUser(UUID userId, PagingParams pagingParams);

    Page<Pass> findPassesByTerritory(UUID terId, PagingParams pagingParams);

    Pass updatePass(Pass pass);

    Pass cancelPass(UUID id);

    Pass activateCancelledPass(UUID id);

    Pass unWarningPass(UUID id);

    void markFavorite(UUID id);

    void unmarkFavorite(UUID id);

    void deletePass(UUID id);

}
