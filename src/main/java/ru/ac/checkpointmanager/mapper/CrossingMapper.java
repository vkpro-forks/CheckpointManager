package ru.ac.checkpointmanager.mapper;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.service.checkpoints.CheckpointService;
import ru.ac.checkpointmanager.service.passes.PassService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
        return modelMapper.map(crossingDTO, Crossing.class);
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


                //crossingDTO у нас source, destination Crossing
                //мы берез из сорса - айди, достаем чекпоинт - и сажаем его в в Crossing,
                // туда нам надо посадить объект Checkpoint
                using(ctx -> {
                    UUID checkpointId = ((CrossingDTO) ctx.getSource()).getCheckpointId();
                    //это временно
                    return checkpointService.findCheckpointById(checkpointId); // тут вернется теперь ДТО чекпоинта, надо проверить как будет работать,
                    // если что метод который сущность возвращает сделать публичным и добавить в интерфейс
                }).map(source, destination.getCheckpoint());

                using(ctx -> {
                    //тут та же история, мы должны цеплять не ДТО а сущность в destination, пока временный вариант
                    UUID passId = ((CrossingDTO) ctx.getSource()).getPassId();
                    return passService.findPassById(passId);
                }).map(source, destination.getPass());
            }
        };

        modelMapper.addMappings(crossingMap);
    }

}
