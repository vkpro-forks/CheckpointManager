package ru.ac.checkpointmanager.service.passes;

import org.springframework.lang.NonNull;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.model.passes.Pass;

public interface PassResolver {

    Pass createPass(@NonNull PassCreateDTO passCreateDTO);

    Pass updatePass(@NonNull PassUpdateDTO passUpdateDTO, @NonNull Pass existPass);
}
