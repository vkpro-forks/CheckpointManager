package ru.ac.checkpointmanager.service.passes;

import org.springframework.data.domain.Page;
import ru.ac.checkpointmanager.dto.passes.FilterParams;
import ru.ac.checkpointmanager.dto.passes.PagingParams;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassResponseDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.model.passes.Pass;

import java.util.UUID;

public interface PassService {

    PassResponseDTO addPass(PassCreateDTO passCreateDTO);

    Page<PassResponseDTO> findPasses(PagingParams pagingParams, FilterParams filterParams);

    Page<PassResponseDTO> findPassesByPartOfVisitorNameAndCarNumber(PagingParams pagingParams,
                                                                    FilterParams filterParams,
                                                                    String part);

    PassResponseDTO findById(UUID id);

    Pass findPassById(UUID passId);

    Page<PassResponseDTO> findPassesByUser(UUID userId, PagingParams pagingParams, FilterParams filterParams);

    Page<PassResponseDTO> findPassesByTerritory(UUID terId, PagingParams pagingParams, FilterParams filterParams);

    PassResponseDTO updatePass(PassUpdateDTO passUpdateDTO);

    PassResponseDTO cancelPass(UUID id);

    PassResponseDTO activateCancelledPass(UUID id);

    PassResponseDTO unWarningPass(UUID id);

    void markFavorite(UUID id);

    void unmarkFavorite(UUID id);

    void deletePass(UUID id);

}
