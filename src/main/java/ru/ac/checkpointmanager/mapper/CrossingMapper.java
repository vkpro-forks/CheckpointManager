package ru.ac.checkpointmanager.mapper;

import jakarta.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.exception.CheckpointNotFoundException;
import ru.ac.checkpointmanager.exception.PassNotFoundException;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.repository.CheckpointRepository;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.service.checkpoints.CheckpointService;
import ru.ac.checkpointmanager.service.passes.PassService;
import ru.ac.checkpointmanager.service.passes.PassServiceImpl;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrossingMapper {

    private final ModelMapper modelMapper;
    private final PassService passService;
    private final CheckpointService checkpointService;


    public Crossing toCrossing(CrossingDTO crossingDTO) {
        log.debug("Mapping CrossingDTO to Crossing");
        Crossing crossing = modelMapper.map(crossingDTO, Crossing.class);

        UUID passId = crossingDTO.getPassId();
        log.debug("Fetching Pass with ID: {}", passId);
        Pass pass = passService.findPassById(passId);
        crossing.setPass(pass);

        UUID checkpointId = crossingDTO.getCheckpointId();
        log.debug("Fetching Checkpoint with ID: {}", checkpointId);
        Checkpoint checkpoint = checkpointService.findCheckpointById(checkpointId);
        crossing.setCheckpoint(checkpoint);

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

    @PostConstruct
    private void configureModelMapper() {
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.addMappings(new PropertyMap<CrossingDTO, Crossing>() {
            @Override
            protected void configure() {
                skip(destination.getPass());
                skip(destination.getCheckpoint());
            }
        });
        log.debug("ModelMapper has been configured");
    }
}
