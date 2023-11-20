package ru.ac.checkpointmanager.mapper;


import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.model.Visitor;

import java.util.Collection;
import java.util.List;

@Component
@Slf4j
public class VisitorMapper {

    private final ModelMapper modelMapper;

    public VisitorMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Visitor toVisitor(VisitorDTO visitorDTO) {
        log.info("Converting from VisitorDTO to Visitor");
        return modelMapper.map(visitorDTO, Visitor.class);
    }

    public VisitorDTO toVisitorDTO(Visitor visitor) {
        log.info("Converting from Visitor to VisitorDTO");
        return modelMapper.map(visitor, VisitorDTO.class);
    }

    public List<VisitorDTO> toVisitorDTOS(Collection<Visitor> people) {
        log.info("Converting a list of Visitor objects to a list of VisitorDTOs");
        return people.stream()
                .map(e -> modelMapper.map(e, VisitorDTO.class))
                .toList();
    }
}
