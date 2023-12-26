package ru.ac.checkpointmanager.service.passes;

import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.model.passes.Pass;

public interface PassResolver {

    Pass createPass(PassCreateDTO passCreateDTO);

}
