package ru.ac.checkpointmanager.mapper;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.exception.CheckpointNotFoundException;
import ru.ac.checkpointmanager.exception.PassNotFoundException;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.repository.CheckpointRepository;
import ru.ac.checkpointmanager.repository.PassRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class CrossingMapper {

    //сделай уже что-нибудь с этим методом!

    private final ModelMapper modelMapper;
    private final PassRepository passRepository;
    private final CheckpointRepository checkpointRepository;

    public CrossingMapper(ModelMapper modelMapper, PassRepository passRepository, CheckpointRepository checkpointRepository) {
        this.modelMapper = modelMapper;
        this.passRepository = passRepository;
        this.checkpointRepository = checkpointRepository;
    }

    public Crossing toCrossing(CrossingDTO crossingDTO) {
        Crossing crossing = new Crossing();
        UUID passId = crossingDTO.getPassId();
        Optional<Pass> optionalPass = passRepository.findById(passId);
        Pass pass = optionalPass.orElseThrow(
                () -> {
                    log.warn("[Pass with id: {}] not found", passId);
                    return new PassNotFoundException("Pass with id: %s not found".formatted(passId));
                });

        Optional<Checkpoint> optionalCheckpoint = checkpointRepository.findById(crossingDTO.getCheckpointId());
        Checkpoint checkpoint = optionalCheckpoint.orElseThrow(
                () -> {
                    log.warn("[Checkpoint with id: {}] not found", crossingDTO.getCheckpointId());
                    return new CheckpointNotFoundException("Checkpoint with id %s not found"
                            .formatted(crossingDTO.getCheckpointId()));
                });
        crossing.setPass(pass);
        crossing.setCheckpoint(checkpoint);
        crossing.setDirection(crossingDTO.getDirection());

        return crossing;
    }

    public CrossingDTO toCrossingDTO(Crossing crossing) {
        return modelMapper.map(crossing, CrossingDTO.class);
    }

    public List<CrossingDTO> toCrossingsDTO(Collection<Crossing> crossings) {
        return crossings.stream()
                .map(crossing -> modelMapper.map(crossing, CrossingDTO.class))
                .toList();
    }
}
