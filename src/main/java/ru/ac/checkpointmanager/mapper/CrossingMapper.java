package ru.ac.checkpointmanager.mapper;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;

import java.util.Collection;
import java.util.List;

@Slf4j
@Component
public class CrossingMapper {

    private final ModelMapper modelMapper;

    @Autowired
    public CrossingMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        configureModelMapper();
    }

    public Crossing toCrossing(CrossingDTO crossingDTO, Pass pass, Checkpoint checkpoint) {
       Crossing crossing = modelMapper.map(crossingDTO, Crossing.class);
       crossing.setPass(pass);
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

    private void configureModelMapper() {
        PropertyMap<CrossingDTO, Crossing> crossingMap = new PropertyMap<>() {
            @Override
            protected void configure() {
                skip(destination.getId());
                skip(destination.getPass());
                skip(destination.getCheckpoint());
            }
        };
        modelMapper.addMappings(crossingMap);
    }

}
