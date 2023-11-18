package ru.ac.checkpointmanager.mapper;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.passes.PassDtoCreate;
import ru.ac.checkpointmanager.dto.passes.PassDtoResponse;
import ru.ac.checkpointmanager.dto.passes.PassDtoUpdate;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassWalk;
import ru.ac.checkpointmanager.service.territories.TerritoryService;
import ru.ac.checkpointmanager.service.user.UserService;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class PassMapper {

    private final ModelMapper modelMapper;
    private final UserService userService;
    private final TerritoryService territoryService;

    @Autowired
    public PassMapper(UserService userService, TerritoryService territoryService) {
        this.modelMapper = new ModelMapper();
        this.userService = userService;
        this.territoryService = territoryService;
        configureModelMapper();
    }

    public Pass toPass(PassDtoCreate passDTOcreate) {

        if (passDTOcreate.getCar() != null) {
            return modelMapper.map(passDTOcreate, PassAuto.class);
        } else if (passDTOcreate.getPerson() != null) {
            return modelMapper.map(passDTOcreate, PassWalk.class);
        }
        throw new IllegalArgumentException("Ошибка при конвертации passDTO (не содержит car или person)");
    }

    public Pass toPass(PassDtoUpdate passDtoUpdate) {
        Pass pass;

        if (passDtoUpdate.getCar() != null) {
            pass = modelMapper.map(passDtoUpdate, PassAuto.class);
        } else if (passDtoUpdate.getPerson() != null) {
            pass = modelMapper.map(passDtoUpdate, PassWalk.class);
        } else {
            throw new IllegalArgumentException("Ошибка при конвертации passDTO (не содержит car или person)");
        }

        pass.setUser(userService.findByPassId(passDtoUpdate.getId()));
        pass.setTerritory(territoryService.findByPassId(passDtoUpdate.getId()));
        return pass;
    }

    public PassDtoResponse toPassDTO(Pass pass) {
        return modelMapper.map(pass, PassDtoResponse.class);
    }

    public List<PassDtoResponse> toPassDTO(List<Pass> pass) {
        return pass.stream()
                .map(e -> modelMapper.map(e, PassDtoResponse.class))
                .toList();
    }

    private void configureModelMapper() {
        PropertyMap<PassDtoCreate, PassAuto> passAutoMapCreate = new PropertyMap<PassDtoCreate, PassAuto>() {
            @Override
            protected void configure() {
                map().setId(UUID.randomUUID());
            }
        };

        PropertyMap<PassDtoCreate, PassWalk> passWalkMapCreate = new PropertyMap<PassDtoCreate, PassWalk>() {
            @Override
            protected void configure() {
                map().setId(UUID.randomUUID());
            }
        };

        modelMapper.addMappings(passAutoMapCreate);
        modelMapper.addMappings(passWalkMapCreate);
    }
}