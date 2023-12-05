package ru.ac.checkpointmanager.mapper;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassResponseDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassWalk;

@Component
@Slf4j
public class PassMapper {

    private final ModelMapper modelMapper;

    @Autowired
    public PassMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        configureModelMapper();
    }

    public Pass toPass(PassCreateDTO passCreateDTO) {

        if (passCreateDTO.getCar() != null) {
            return modelMapper.map(passCreateDTO, PassAuto.class);
        } else {
            return modelMapper.map(passCreateDTO, PassWalk.class);
        }
    }

    public Pass toPass(PassUpdateDTO passUpdateDTO, User user, Territory territory) {
        Pass pass;

        if (passUpdateDTO.getCar() != null) {
            pass = modelMapper.map(passUpdateDTO, PassAuto.class);
        } else {
            pass = modelMapper.map(passUpdateDTO, PassWalk.class);
        }
        pass.setUser(user);
        pass.setTerritory(territory);

        return pass;
    }

    public PassResponseDTO toPassDTO(Pass pass) {
        return modelMapper.map(pass, PassResponseDTO.class);
    }

    private void configureModelMapper() {
        PropertyMap<PassCreateDTO, PassAuto> passAutoMapCreate = new PropertyMap<>() {
            @Override
            protected void configure() {
                map().setId(null);
            }
        };
        modelMapper.addMappings(passAutoMapCreate);

        PropertyMap<PassCreateDTO, PassWalk> passWalkMapCreate = new PropertyMap<>() {
            @Override
            protected void configure() {
                map().setId(null);
            }
        };
        modelMapper.addMappings(passWalkMapCreate);
    }
}