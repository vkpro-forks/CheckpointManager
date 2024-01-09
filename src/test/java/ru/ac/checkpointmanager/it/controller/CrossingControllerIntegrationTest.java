package ru.ac.checkpointmanager.it.controller;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.RedisAndPostgresTestContainersConfiguration;
import ru.ac.checkpointmanager.dto.CrossingRequestDTO;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.checkpoints.CheckpointType;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.repository.CheckpointRepository;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.repository.car.CarRepository;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = {"ADMIN"})
class CrossingControllerIntegrationTest extends RedisAndPostgresTestContainersConfiguration {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TerritoryRepository territoryRepository;

    @Autowired
    CarRepository carRepository;

    @Autowired
    PassRepository passRepository;

    @Autowired
    CarBrandRepository carBrandRepository;

    @Autowired
    CheckpointRepository checkpointRepository;

    @AfterEach
    void clear() {
        passRepository.deleteAll();
        carRepository.deleteAll();
        carBrandRepository.deleteAll();
        checkpointRepository.deleteAll();
        territoryRepository.deleteAll();
        userRepository.deleteAll();
    }


    @Test
    @SneakyThrows
    void shouldAddCrossingForInDirection() {
        //save pass
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        CarBrand carBrand = TestUtils.getCarBrand();
        CarBrand savedCarBrand = carBrandRepository.saveAndFlush(carBrand);
        Car car = TestUtils.getCar(savedCarBrand);
        car.setBrand(savedCarBrand);
        Car savedCar = carRepository.saveAndFlush(car);

        PassAuto pass = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar);
        PassAuto savedPass = passRepository.saveAndFlush(pass);

        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.AUTO, savedTerritory);
        Checkpoint savedCheckPoint = checkpointRepository.saveAndFlush(checkpoint);

        CrossingRequestDTO crossingRequestDTO = new CrossingRequestDTO(savedPass.getId(),
                savedCheckPoint.getId(),
                ZonedDateTime.now());

        String crossingDtoString = TestUtils.jsonStringFromObject(crossingRequestDTO);

        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_URL + "/in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crossingDtoString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.direction").value(Direction.IN.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.performedAt").isNotEmpty());

        Optional<Pass> passOptional = passRepository.findById(savedPass.getId());
        Assertions.assertThat(passOptional).isPresent();
        Assertions.assertThat(passOptional.get().getExpectedDirection()).isEqualTo(Direction.OUT);
    }

    @Test
    @SneakyThrows
    void shouldAddCrossingForOutDirection() {
        //save pass
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        CarBrand carBrand = TestUtils.getCarBrand();
        CarBrand savedCarBrand = carBrandRepository.saveAndFlush(carBrand);
        Car car = TestUtils.getCar(savedCarBrand);
        car.setBrand(savedCarBrand);
        Car savedCar = carRepository.saveAndFlush(car);

        PassAuto pass = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar);
        PassAuto savedPass = passRepository.saveAndFlush(pass);

        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.AUTO, savedTerritory);
        Checkpoint savedCheckPoint = checkpointRepository.saveAndFlush(checkpoint);

        CrossingRequestDTO crossingRequestDTO = new CrossingRequestDTO(savedPass.getId(),
                savedCheckPoint.getId(),
                ZonedDateTime.now());

        String crossingDtoString = TestUtils.jsonStringFromObject(crossingRequestDTO);

        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_URL + "/out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crossingDtoString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.direction").value(Direction.OUT.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.performedAt").isNotEmpty());

        Optional<Pass> passOptional = passRepository.findById(savedPass.getId());
        Assertions.assertThat(passOptional).isPresent();
        Assertions.assertThat(passOptional.get().getExpectedDirection()).isEqualTo(Direction.IN);
    }

}
