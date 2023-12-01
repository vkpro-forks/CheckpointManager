package ru.ac.checkpointmanager.mapper;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassResponseDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
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

    public Pass toPass(PassCreateDTO passDTOcreateDTO) {

        if (passDTOcreateDTO.getCar() != null) {
            return modelMapper.map(passDTOcreateDTO, PassAuto.class);
        } else {
            return modelMapper.map(passDTOcreateDTO, PassWalk.class);
        }
    }

    public Pass toPass(PassUpdateDTO passUpdateDTO) {
        Pass pass;

        if (passUpdateDTO.getCar() != null) {
            pass = modelMapper.map(passUpdateDTO, PassAuto.class);
        } else {
            pass = modelMapper.map(passUpdateDTO, PassWalk.class);
        }

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

        PropertyMap<PassUpdateDTO, PassAuto> passAutoMapUpdate = new PropertyMap<>() {
            @Override
            protected void configure() {
                using(ctx -> {
                    UUID passId = ((PassUpdateDTO) ctx.getSource()).getId();
                    return userService.findByPassId(passId);
                }).map(source, destination.getUser());

                using(ctx -> {
                    UUID passId = ((PassUpdateDTO) ctx.getSource()).getId();
                    return territoryService.findByPassId(passId);
                }).map(source, destination.getTerritory());
            }
        };
        modelMapper.addMappings(passAutoMapUpdate);

        PropertyMap<PassUpdateDTO, PassWalk> passWalkMapUpdate = new PropertyMap<>() {
            @Override
            protected void configure() {
                using(ctx -> {
                    UUID passId = ((PassUpdateDTO) ctx.getSource()).getId();
                    return userService.findByPassId(passId);
                }).map(source, destination.getUser());

                using(ctx -> {
                    UUID passId = ((PassUpdateDTO) ctx.getSource()).getId();
                    return territoryService.findByPassId(passId);
                }).map(source, destination.getTerritory());
            }
        };
        modelMapper.addMappings(passWalkMapUpdate);
    }
}