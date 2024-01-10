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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.RedisAndPostgresTestContainersConfiguration;
import ru.ac.checkpointmanager.dto.CrossingRequestDTO;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.handler.ErrorCode;
import ru.ac.checkpointmanager.exception.handler.ErrorMessage;
import ru.ac.checkpointmanager.model.Crossing;
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
import ru.ac.checkpointmanager.repository.CrossingRepository;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.repository.car.CarRepository;
import ru.ac.checkpointmanager.service.crossing.CrossingPassHandler;
import ru.ac.checkpointmanager.service.crossing.impl.PassProcessorOnetime;
import ru.ac.checkpointmanager.service.crossing.impl.PassProcessorPermanent;
import ru.ac.checkpointmanager.util.TestMessage;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    @Autowired
    CrossingRepository crossingRepository;

    @Autowired
    CrossingPassHandler crossingPassHandler;

    @Autowired
    PassProcessorOnetime passProcessingOnetime;

    @Autowired
    PassProcessorPermanent passProcessingPermanent;

    @AfterEach
    void clear() {
        passRepository.deleteAll();
        carRepository.deleteAll();
        carBrandRepository.deleteAll();
        checkpointRepository.deleteAll();
        territoryRepository.deleteAll();
        userRepository.deleteAll();
        crossingRepository.deleteAll();
        ReflectionTestUtils.setField(crossingPassHandler, "passProcessingMap",
                Map.of(
                        "ONETIME", passProcessingOnetime,
                        "PERMANENT", passProcessingPermanent
                ));
    }


    @Test
    @SneakyThrows
    void addCrossing_InDirection_ReturnCrossingDTO() {
        //given
        Pass savedPass = setupAndSavePass(PassStatus.ACTIVE);
        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.AUTO, savedPass.getTerritory());
        Checkpoint savedCheckPoint = checkpointRepository.saveAndFlush(checkpoint);
        CrossingRequestDTO crossingRequestDTO = new CrossingRequestDTO(savedPass.getId(),
                savedCheckPoint.getId(),
                ZonedDateTime.now());
        String crossingDtoString = TestUtils.jsonStringFromObject(crossingRequestDTO);
        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_URL + "/in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(crossingDtoString));
        //then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.direction").value(Direction.IN.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.performedAt").isNotEmpty());
        Optional<Pass> passOptional = passRepository.findById(savedPass.getId());
        Assertions.assertThat(passOptional).isPresent();
        Assertions.assertThat(passOptional.get().getExpectedDirection()).isEqualTo(Direction.OUT);
    }

    @Test
    @SneakyThrows
    void addCrossing_OutDirection_ReturnCrossingDTO() {
        //given
        Pass savedPass = setupAndSavePass(PassStatus.ACTIVE);
        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.AUTO, savedPass.getTerritory());
        Checkpoint savedCheckPoint = checkpointRepository.saveAndFlush(checkpoint);
        CrossingRequestDTO crossingRequestDTO = new CrossingRequestDTO(savedPass.getId(),
                savedCheckPoint.getId(),
                ZonedDateTime.now());
        String crossingDtoString = TestUtils.jsonStringFromObject(crossingRequestDTO);
        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_URL + "/out")
                .contentType(MediaType.APPLICATION_JSON)
                .content(crossingDtoString));
        //then
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
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
        //given
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
        //when
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST, UrlConstants.CROSSING_URL + direction);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_URL + direction)
                .content(crossingDto)
                .contentType(MediaType.APPLICATION_JSON));
        //then
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.PASS_NOT_FOUND.formatted(TestUtils.PASS_ID)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/in", "/out"})
    @SneakyThrows
    void addCrossing_PassInactive_HandleErrorAndReturnBadRequest(String direction) {
        //given
        Pass savedPass = setupAndSavePass(PassStatus.OUTDATED);
        CrossingRequestDTO crossingRequestDTO = new CrossingRequestDTO(savedPass.getId(),
                TestUtils.CHECKPOINT_ID, //doesn't matter because pass inactive
                ZonedDateTime.now());
        String crossingDtoString = TestUtils.jsonStringFromObject(crossingRequestDTO);
        //when
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST, UrlConstants.CROSSING_URL + direction);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_URL + direction)
                .contentType(MediaType.APPLICATION_JSON)
                .content(crossingDtoString));
        //then
        resultActions.andExpect(MockMvcResultMatchers.status().isBadRequest())
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
        //given
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
        //when
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST, UrlConstants.CROSSING_URL + direction);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_URL + direction)
                .contentType(MediaType.APPLICATION_JSON)
                .content(crossingDtoString));
        //then
        resultActions.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers
                        .jsonPath(TestUtils.JSON_ERROR_CODE).value(ErrorCode.BAD_REQUEST.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(ExceptionUtils.PASS_MISMATCHED_TERRITORY.formatted(savedPass.getId(),
                                savedPass.getTerritory(), savedAnotherTerritory)))
                .andExpect(MockMvcResultMatchers
                        .jsonPath(TestUtils.JSON_TITLE).value(ErrorMessage.PASS_EXCEPTION_TITLE));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/in", "/out"})
    @SneakyThrows
    void addCrossing_UnsupportedPassProcessor_HandleErrorAndReturnInternalServerError(String direction) {
        //given
        log.info("Delete Pass processors from map");
        ReflectionTestUtils.setField(crossingPassHandler, "passProcessingMap", Collections.emptyMap());
        Pass savedPass = setupAndSavePass(PassStatus.ACTIVE);
        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.AUTO, savedPass.getTerritory());
        Checkpoint savedCheckPoint = checkpointRepository.saveAndFlush(checkpoint);
        CrossingRequestDTO crossingRequestDTO = new CrossingRequestDTO(savedPass.getId(),
                savedCheckPoint.getId(),
                ZonedDateTime.now());
        String crossingDtoString = TestUtils.jsonStringFromObject(crossingRequestDTO);
        //when
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST, UrlConstants.CROSSING_URL + direction);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_URL + direction)
                .contentType(MediaType.APPLICATION_JSON)
                .content(crossingDtoString));
        //then
        resultActions.andExpect(MockMvcResultMatchers.status().isInternalServerError())
                .andExpect(MockMvcResultMatchers
                        .jsonPath(TestUtils.JSON_ERROR_CODE).value(ErrorCode.INTERNAL_SERVER_ERROR.toString()))
                .andExpect(MockMvcResultMatchers
                        .jsonPath(TestUtils.JSON_TITLE).value(ErrorMessage.INTERNAL_SERVER_ERROR))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(ExceptionUtils.UNSUPPORTED_PASS_TYPE.formatted(savedPass.getTimeType())));
    }

    @Test
    @SneakyThrows
    void addCrossing_OneTimePassAlreadyUsed_HandleErrorAndReturnBadRequest() {
        //given
        Pass savedPass = setupAndSavePass(PassStatus.ACTIVE);
        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.AUTO, savedPass.getTerritory());
        Checkpoint savedCheckPoint = checkpointRepository.saveAndFlush(checkpoint);
        Crossing crossing = TestUtils.getCrossing(savedPass, savedCheckPoint, Direction.OUT);
        crossingRepository.saveAndFlush(crossing);
        CrossingRequestDTO crossingRequestDTO = new CrossingRequestDTO(savedPass.getId(),
                savedCheckPoint.getId(),
                ZonedDateTime.now());
        String crossingDtoString = TestUtils.jsonStringFromObject(crossingRequestDTO);
        //when
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST, UrlConstants.CROSSING_URL + "/in");
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_URL + "/in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(crossingDtoString));
        //then
        resultActions.andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers
                        .jsonPath(TestUtils.JSON_ERROR_CODE).value(ErrorCode.CONFLICT.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(ExceptionUtils.PASS_ALREADY_USED.formatted(savedPass.getId())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/in", "/out"})
    @SneakyThrows
    void addCrossing_CheckpointNotExists_HandleErrorAndReturnNotFound(String direction) {
        //given
        Pass savedPass = setupAndSavePass(PassStatus.ACTIVE);
        CrossingRequestDTO crossingRequestDTO = new CrossingRequestDTO(savedPass.getId(),
                TestUtils.CHECKPOINT_ID, //not in repository
                ZonedDateTime.now());
        String crossingDto = TestUtils.jsonStringFromObject(crossingRequestDTO);
        //when
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST, UrlConstants.CROSSING_URL + direction);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_URL + direction)
                .content(crossingDto)
                .contentType(MediaType.APPLICATION_JSON));
        //then
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.CHECKPOINT_NOT_FOUND.formatted(TestUtils.CHECKPOINT_ID)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    private Pass setupAndSavePass(PassStatus passStatus) {
        log.info("Saving Territory, User, Car, Brand, and Pass with status {}", passStatus);
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
