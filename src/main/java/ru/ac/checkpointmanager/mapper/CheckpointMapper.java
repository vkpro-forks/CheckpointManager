package ru.ac.checkpointmanager.mapper;


import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.CheckpointDTO;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;

import java.util.List;

@Component
@Slf4j
public class CheckpointMapper {
    private final ModelMapper modelMapper;

    @Autowired
    public CheckpointMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Checkpoint toCheckpoint(CheckpointDTO checkpointDTO) {
        return modelMapper.map(checkpointDTO, Checkpoint.class);
    }

    public CheckpointDTO toCheckpointDTO(Checkpoint checkpoint) {
        return modelMapper.map(checkpoint, CheckpointDTO.class);
    }

    public List<CheckpointDTO> toCheckpointsDTO(List<Checkpoint> checkpoints) {
        return checkpoints.stream()
                .map(e -> modelMapper.map(e, CheckpointDTO.class))
                .toList();
    }
}
