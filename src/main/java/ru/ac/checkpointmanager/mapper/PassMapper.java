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

import java.util.UUID;

@Component
@Slf4j
public class    PassMapper {

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
        } else {
            return modelMapper.map(passDTOcreate, PassWalk.class);
        }
    }

    public Pass toPass(PassDtoUpdate passDtoUpdate) {
        Pass pass;

        if (passDtoUpdate.getCar() != null) {
            pass = modelMapper.map(passDtoUpdate, PassAuto.class);
        } else {
            pass = modelMapper.map(passDtoUpdate, PassWalk.class);
        }

        return pass;
    }

    public PassDtoResponse toPassDTO(Pass pass) {
        return modelMapper.map(pass, PassDtoResponse.class);
    }

    private void configureModelMapper() {
        PropertyMap<PassDtoCreate, PassAuto> passAutoMapCreate = new PropertyMap<>() {
            @Override
            protected void configure() {
                map().setId(null);
            }
        };
        modelMapper.addMappings(passAutoMapCreate);

        PropertyMap<PassDtoCreate, PassWalk> passWalkMapCreate = new PropertyMap<>() {
            @Override
            protected void configure() {
                map().setId(null);
            }
        };
        modelMapper.addMappings(passWalkMapCreate);

        PropertyMap<PassDtoUpdate, PassAuto> passAutoMapUpdate = new PropertyMap<>() {
            @Override
            protected void configure() {
                using(ctx -> {
                    UUID passId = ((PassDtoUpdate) ctx.getSource()).getId();
                    return userService.findByPassId(passId);
                }).map(source, destination.getUser());

                using(ctx -> {
                    UUID passId = ((PassDtoUpdate) ctx.getSource()).getId();
                    return territoryService.findByPassId(passId);
                }).map(source, destination.getTerritory());
            }
        };
        modelMapper.addMappings(passAutoMapUpdate);

        PropertyMap<PassDtoUpdate, PassWalk> passWalkMapUpdate = new PropertyMap<>() {
            @Override
            protected void configure() {
                using(ctx -> {
                    UUID passId = ((PassDtoUpdate) ctx.getSource()).getId();
                    return userService.findByPassId(passId);
                }).map(source, destination.getUser());

                using(ctx -> {
                    UUID passId = ((PassDtoUpdate) ctx.getSource()).getId();
                    return territoryService.findByPassId(passId);
                }).map(source, destination.getTerritory());
            }
        };
        modelMapper.addMappings(passWalkMapUpdate);
    }
}