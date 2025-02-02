package ru.ac.checkpointmanager.service.passes;

import org.springframework.data.domain.Page;
import ru.ac.checkpointmanager.dto.passes.PassFilterParams;
import ru.ac.checkpointmanager.dto.passes.PagingParams;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassResponseDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.model.passes.Pass;

import java.util.UUID;

public interface PassService {

    PassResponseDTO addPass(PassCreateDTO passCreateDTO);

    Page<PassResponseDTO> findPasses(PagingParams pagingParams, PassFilterParams passFilterParams, String part);

    PassResponseDTO findById(UUID id);

    Pass findPassById(UUID passId);

    Page<PassResponseDTO> findPassesByUser(UUID userId, PagingParams pagingParams, PassFilterParams passFilterParams, String part);

    Page<PassResponseDTO> findPassesByTerritory(UUID terId, PagingParams pagingParams, PassFilterParams passFilterParams, String part);

    Page<PassResponseDTO> findPassesByUsersTerritories(UUID userId, PagingParams pagingParams, PassFilterParams passFilterParams,
                                                       String part);

    PassResponseDTO updatePass(PassUpdateDTO passUpdateDTO);

    PassResponseDTO cancelPass(UUID id);

    PassResponseDTO activateCancelledPass(UUID id);

    PassResponseDTO unWarningPass(UUID id);

    void markFavorite(UUID id);

    void unmarkFavorite(UUID id);

    void deletePass(UUID id);

}
