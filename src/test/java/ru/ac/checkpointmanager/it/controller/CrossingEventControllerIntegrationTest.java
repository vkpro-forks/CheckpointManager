package ru.ac.checkpointmanager.it.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.ac.checkpointmanager.config.EnablePostgresAndRedisTestContainers;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
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
import ru.ac.checkpointmanager.util.ResultCheckUtils;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
@EnablePostgresAndRedisTestContainers
class CrossingEventControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PassRepository passRepository;

    @Autowired
    TerritoryRepository territoryRepository;

    @Autowired
    CarRepository carRepository;

    @Autowired
    CarBrandRepository carBrandRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CrossingRepository crossingRepository;

    @Autowired
    CheckpointRepository checkpointRepository;

    Territory savedTerritory;

    User savedUser;

    Car savedCar;

    CarBrand savedCarBrand;

    @AfterEach
    void clear() {
        passRepository.deleteAll();
        carRepository.deleteAll();
        carBrandRepository.deleteAll();
        checkpointRepository.deleteAll();
        territoryRepository.deleteAll();
        userRepository.deleteAll();
        crossingRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    @WithMockUser(roles = {"ADMIN"})
    void getEventsByUserId_AllOk_ReturnPageWithObjects() {
        Pass savedPass = setupAndSavePass();
        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.AUTO, savedPass.getTerritory());
        Checkpoint savedCheckPoint = checkpointRepository.saveAndFlush(checkpoint);
        crossingRepository.saveAndFlush(TestUtils.getCrossing(savedPass, savedCheckPoint, Direction.IN));
        crossingRepository.saveAndFlush(TestUtils.getCrossing(savedPass, savedCheckPoint, Direction.OUT));

        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(UrlConstants.EVENT_USER_URL, savedUser.getId()));

        checkEventFields(resultActions, savedPass);
    }

    @Test
    @SneakyThrows
    @WithMockUser(roles = {"ADMIN"})
    void getEventsByUserId_UserNotFound_ReturnPageWithObjects() {
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(UrlConstants.EVENT_USER_URL, TestUtils.USER_ID));

        resultActions.andExpect(status().isNotFound())
                .andExpectAll(jsonPath(TestUtils.JSON_DETAIL)
                        .value(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(TestUtils.USER_ID)));
        ResultCheckUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    @WithMockUser(roles = {"ADMIN"})
    void getEventsByTerritoryId_AllOk_ReturnPageWithObjects() {
        Pass savedPass = setupAndSavePass();
        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.AUTO, savedPass.getTerritory());
        Checkpoint savedCheckPoint = checkpointRepository.saveAndFlush(checkpoint);
        crossingRepository.saveAndFlush(TestUtils.getCrossing(savedPass, savedCheckPoint, Direction.IN));
        crossingRepository.saveAndFlush(TestUtils.getCrossing(savedPass, savedCheckPoint, Direction.OUT));

        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(UrlConstants.EVENT_TERRITORY_URL, savedTerritory.getId()));

        checkEventFields(resultActions, savedPass);
    }

    @Test
    @SneakyThrows
    @WithMockUser(roles = {"ADMIN"})
    void getEventsByTerritoryId_TerritoryNotFound_ReturnPageWithObjects() {
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(UrlConstants.EVENT_TERRITORY_URL, TestUtils.TERR_ID));

        resultActions.andExpect(status().isNotFound())
                .andExpectAll(jsonPath(TestUtils.JSON_DETAIL)
                        .value(ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(TestUtils.TERR_ID)));
        ResultCheckUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    @WithMockUser(roles = {"ADMIN"})
    void getAllEvents_AllOk_ReturnPageWithObjects() {
        Pass savedPass = setupAndSavePass();
        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.AUTO, savedPass.getTerritory());
        Checkpoint savedCheckPoint = checkpointRepository.saveAndFlush(checkpoint);
        crossingRepository.saveAndFlush(TestUtils.getCrossing(savedPass, savedCheckPoint, Direction.IN));
        crossingRepository.saveAndFlush(TestUtils.getCrossing(savedPass, savedCheckPoint, Direction.OUT));

        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(UrlConstants.EVENT_ALL_URL));

        checkEventFields(resultActions, savedPass);
    }

    @Test
    @SneakyThrows
    @WithMockUser(roles = {"MANAGER"})
    void findEventsByUsersTerritories_AllOk_ReturnPageWithObjects() {
        Pass savedPass = setupAndSavePass();
        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.AUTO, savedPass.getTerritory());
        Checkpoint savedCheckPoint = checkpointRepository.saveAndFlush(checkpoint);
        crossingRepository.saveAndFlush(TestUtils.getCrossing(savedPass, savedCheckPoint, Direction.IN));
        crossingRepository.saveAndFlush(TestUtils.getCrossing(savedPass, savedCheckPoint, Direction.OUT));

        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(UrlConstants.EVENT_URL + "/user/{userId}/territories", savedUser.getId()));

        checkEventFields(resultActions, savedPass);
    }

    @Test
    @SneakyThrows
    @WithMockUser(roles = {"MANAGER"})
    void findEventsByUsersTerritories_UserNotFound_ReturnPageWithObjects() {
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(UrlConstants.EVENT_URL + "/user/{userId}/territories", TestUtils.USER_ID));

        resultActions.andExpect(status().isNotFound())
                .andExpectAll(jsonPath(TestUtils.JSON_DETAIL)
                        .value(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(TestUtils.USER_ID)));
        ResultCheckUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    @WithMockUser(roles = {"MANAGER"})
    void findEventsByUsersTerritories_TerritoryNotFound_ReturnPageWithObjects() {
        User user = TestUtils.getUser();
        User anotherSavedUser = userRepository.saveAndFlush(user);
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(UrlConstants.EVENT_URL + "/user/{userId}/territories", anotherSavedUser.getId()));

        resultActions.andExpect(status().isNotFound());
        ResultCheckUtils.checkNotFoundFields(resultActions);
    }

    private Pass setupAndSavePass() {
        log.info("Saving Territory, User, Car, Brand, and Pass}");
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        savedTerritory = territoryRepository.saveAndFlush(territory);
        CarBrand carBrand = TestUtils.getCarBrand();
        savedCarBrand = carBrandRepository.saveAndFlush(carBrand);
        Car car = TestUtils.getCar(savedCarBrand);
        car.setBrand(savedCarBrand);
        savedCar = carRepository.saveAndFlush(car);

        PassAuto pass = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar);
        pass.setStatus(PassStatus.ACTIVE);
        return passRepository.saveAndFlush(pass);
    }

    private void checkEventFields(ResultActions resultActions, Pass savedPass) throws Exception {
        resultActions.andExpect(status().isOk())
                .andExpectAll(jsonPath("$.content[0].terr_name").value(savedTerritory.getName()),
                        jsonPath("$.content[0].pass_status").value(savedPass.getStatus().name()),
                        jsonPath("$.content[0].pass_id").value(savedPass.getId().toString()),
                        jsonPath("$.content[0].car_number").value(savedCar.getLicensePlate()),
                        jsonPath("$.content[0].pass_time_type").value(savedPass.getTimeType().name()),
                        jsonPath("$.content[0].dtype").value("AUTO"),
                        jsonPath("$.content[0].in_time").isNotEmpty(),
                        jsonPath("$.content[0].out_time").isNotEmpty());
    }


}
