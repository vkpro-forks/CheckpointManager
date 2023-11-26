package ru.ac.checkpointmanager.mapper;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.service.checkpoints.CheckpointService;
import ru.ac.checkpointmanager.service.passes.PassService;

import java.util.Collection;
import java.util.List;

@Slf4j
@Component
public class CrossingMapper {

    private final ModelMapper modelMapper;
    private final PassService passService;
    private final CheckpointService checkpointService;

    @Autowired
    public CrossingMapper(PassService passService, CheckpointService checkpointService) {
        this.modelMapper = new ModelMapper();
        this.passService = passService;
        this.checkpointService = checkpointService;
        configureModelMapper();
    }


    public Crossing toCrossing(CrossingDTO crossingDTO) {
        Crossing crossing = modelMapper.map(crossingDTO, Crossing.class);
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

    private void configureModelMapper() {
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.addMappings(new PropertyMap<Crossing, CrossingDTO>() {
            @Override
            protected void configure() {
                map(source.getPass().getId(), destination.getPassId());
                map(source.getCheckpoint().getId(), destination.getCheckpointId());
            }
        });
        modelMapper.addMappings(new PropertyMap<CrossingDTO, Crossing>() {
            @Override
            protected void configure() {
                using(ctx -> passService.findPassById(((CrossingDTO) ctx.getSource()).getPassId()))
                        .map(source, destination.getPass());
                using(ctx -> checkpointService.findCheckpointById(((CrossingDTO) ctx.getSource()).getCheckpointId()))
                        .map(source, destination.getCheckpoint());
            }
        });
    }
}
