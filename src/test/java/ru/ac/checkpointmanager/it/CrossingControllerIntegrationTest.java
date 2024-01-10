package ru.ac.checkpointmanager.it;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.RedisAndPostgresTestContainersConfiguration;
import ru.ac.checkpointmanager.dto.CrossingRequestDTO;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.handler.ErrorCode;
import ru.ac.checkpointmanager.exception.handler.ErrorMessage;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.checkpoints.CheckpointType;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.repository.CheckpointRepository;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.repository.car.CarRepository;
import ru.ac.checkpointmanager.util.TestMessage;
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
@Slf4j
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
        Pass savedPass = setupAndSavePass(PassStatus.ACTIVE);

        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.AUTO, savedPass.getTerritory());
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
        Pass savedPass = setupAndSavePass(PassStatus.ACTIVE);

        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.AUTO, savedPass.getTerritory());
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

    @ParameterizedTest
    @ValueSource(strings = {"/in", "/out"})
    @SneakyThrows
    void addCrossing_PassNotExists_HandleErrorAndReturnNotFound(String direction) {
        log.info("Saving checkpoint and territory");
        Checkpoint checkpoint = new Checkpoint();
        checkpoint.setName(TestUtils.CHECKPOINT_NAME);
        checkpoint.setType(CheckpointType.AUTO);
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        territoryRepository.save(territory);
        checkpoint.setTerritory(territory);
        Checkpoint savedCheckPoint = checkpointRepository.save(checkpoint);
        CrossingRequestDTO crossingDTO = TestUtils.getCrossingRequestDTO();
        crossingDTO.setCheckpointId(savedCheckPoint.getId());
        String crossingDto = TestUtils.jsonStringFromObject(crossingDTO);

        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST, UrlConstants.CROSSING_URL + direction);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_URL + direction)
                        .content(crossingDto)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(ExceptionUtils.PASS_NOT_FOUND.formatted(TestUtils.PASS_ID)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/in", "/out"})
    @SneakyThrows
    void addCrossing_PassInactive_HandleErrorAndReturnBadRequest(String direction) {
        log.info("Saving territory, car, brand and INACTIVE PASS");
        Pass savedPass = setupAndSavePass(PassStatus.OUTDATED);

        CrossingRequestDTO crossingRequestDTO = new CrossingRequestDTO(savedPass.getId(),
                TestUtils.CHECKPOINT_ID, //doesnt matter because pass inactive
                ZonedDateTime.now());
        String crossingDtoString = TestUtils.jsonStringFromObject(crossingRequestDTO);

        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST, UrlConstants.CROSSING_URL + direction);
        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_URL + direction)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crossingDtoString))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers
                        .jsonPath(TestUtils.JSON_ERROR_CODE).value(ErrorCode.BAD_REQUEST.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(ExceptionUtils.INACTIVE_PASS.formatted(savedPass.getId())))
                .andExpect(MockMvcResultMatchers
                        .jsonPath(TestUtils.JSON_TITLE).value(ErrorMessage.PASS_EXCEPTION_TITLE));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/in", "/out"})
    @SneakyThrows
    void addCrossing_PassAndCheckpointWithDifferentTerritories_HandleErrorAndReturnBadRequest(String direction) {
        log.info("Saving territory, car, brand and INACTIVE PASS");
        Pass savedPass = setupAndSavePass(PassStatus.ACTIVE);
        Territory anotherTerritory = new Territory();
        anotherTerritory.setName("Another territory");
        Territory savedAnotherTerritory = territoryRepository.saveAndFlush(anotherTerritory);
        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.AUTO, savedAnotherTerritory);
        Checkpoint savedCheckPoint = checkpointRepository.saveAndFlush(checkpoint);

        CrossingRequestDTO crossingRequestDTO = new CrossingRequestDTO(savedPass.getId(),
                savedCheckPoint.getId(),
                ZonedDateTime.now());
        String crossingDtoString = TestUtils.jsonStringFromObject(crossingRequestDTO);

        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST, UrlConstants.CROSSING_URL + direction);
        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_URL + direction)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crossingDtoString))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers
                        .jsonPath(TestUtils.JSON_ERROR_CODE).value(ErrorCode.BAD_REQUEST.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(ExceptionUtils.PASS_MISMATCHED_TERRITORY.formatted(savedPass.getId(),
                                savedPass.getTerritory(), savedAnotherTerritory)))
                .andExpect(MockMvcResultMatchers
                        .jsonPath(TestUtils.JSON_TITLE).value(ErrorMessage.PASS_EXCEPTION_TITLE));
    }

    private Pass setupAndSavePass(PassStatus passStatus) {
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
        pass.setStatus(passStatus);
        return passRepository.saveAndFlush(pass);
    }


}
