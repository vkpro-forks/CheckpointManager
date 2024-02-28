package ru.ac.checkpointmanager.it.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.EnablePostgresAndRedisTestContainers;
import ru.ac.checkpointmanager.config.OpenAllEndpointsTestConfiguration;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.checkpoints.CheckpointType;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.repository.CheckpointRepository;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.repository.car.CarRepository;
import ru.ac.checkpointmanager.util.PassTestData;
import ru.ac.checkpointmanager.util.TestMessage;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.List;
import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@Import({OpenAllEndpointsTestConfiguration.class})
@ActiveProfiles("test")
@WithMockUser(roles = {"ADMIN"})
@Slf4j
@EnablePostgresAndRedisTestContainers
class TerritoryControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TerritoryRepository territoryRepository;

    @Autowired
    PassRepository passRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CarRepository carRepository;

    @Autowired
    CarBrandRepository carBrandRepository;

    @Autowired
    CheckpointRepository checkpointRepository;

    @AfterEach
    void clear() {
        carRepository.deleteAll();
        passRepository.deleteAll();
        userRepository.deleteAll();
        territoryRepository.deleteAll();
        carBrandRepository.deleteAll();
        checkpointRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void shouldDeleteTerritoryWithPassesWithoutCheckPoints() {
        Territory territory = TestUtils.getTerritory();
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        CarBrand savedCarBrand = carBrandRepository.saveAndFlush(TestUtils.getCarBrand());
        Car car = TestUtils.getCar(savedCarBrand);
        Car savedCar = carRepository.saveAndFlush(car);
        PassAuto passAuto = PassTestData.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar);
        passRepository.saveAndFlush(passAuto);
        log.info("SAVED territory, user, car brand, car");
        String url = UrlConstants.TERR_URL + "/" + savedTerritory.getId();

        log.info(TestMessage.PERFORM_HTTP, HttpMethod.DELETE, url);
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        log.info("Check that territory was deleted, but passes not");
        Optional<Territory> optionalTerritory = territoryRepository.findById(savedTerritory.getId());
        Assertions.assertThat(optionalTerritory).isEmpty();
        List<Pass> allPasses = passRepository.findAll();
        Assertions.assertThat(allPasses).isEmpty();
    }

    @Test
    @SneakyThrows
    void shouldDeleteTerritoryWithPassesWithCheckPoints() {
        Territory territory = TestUtils.getTerritory();
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        CarBrand savedCarBrand = carBrandRepository.saveAndFlush(TestUtils.getCarBrand());
        Car car = TestUtils.getCar(savedCarBrand);
        Car savedCar = carRepository.saveAndFlush(car);
        PassAuto passAuto = PassTestData.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar);
        passRepository.saveAndFlush(passAuto);
        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.UNIVERSAL, savedTerritory);
        Checkpoint savedCheckPoint = checkpointRepository.saveAndFlush(checkpoint);
        log.info("SAVED territory, user, car brand, car, checkpoint");
        String url = UrlConstants.TERR_URL + "/" + savedTerritory.getId();

        log.info(TestMessage.PERFORM_HTTP, HttpMethod.DELETE, url);
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        log.info("Check that territory with all checkpoints was deleted, but passes not");
        Optional<Territory> optionalTerritory = territoryRepository.findById(savedTerritory.getId());
        Assertions.assertThat(optionalTerritory).isEmpty();
        List<Pass> allPasses = passRepository.findAll();
        Assertions.assertThat(allPasses).isEmpty();
        Optional<Checkpoint> optionalCheckpoint = checkpointRepository.findById(savedCheckPoint.getId());
        Assertions.assertThat(optionalCheckpoint).isEmpty();
    }

    @Test
    @SneakyThrows
    void attachUserToTerritory_ifUserAlreadyAttachedToTerritory_handleExceptionAndReturn409() {
        log.info("Saving user and territory with him");
        Territory territory = TestUtils.getTerritory();
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        String url = UrlConstants.TERR_ATTACH_DETACH_URL
                .formatted(savedTerritory.getId(), savedUser.getId());
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.PATCH, url);
        mockMvc.perform(MockMvcRequestBuilders.patch(url))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith("User")));
    }

    @Test
    @SneakyThrows
    void attachUserToTerritory_ifUserNotConnectedToTerritory_attachAndReturnNoContent() {
        log.info("Saving user and territory");
        Territory territory = TestUtils.getTerritory();
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        String url = UrlConstants.TERR_ATTACH_DETACH_URL
                .formatted(savedTerritory.getId(), savedUser.getId());
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.PATCH, url);
        mockMvc.perform(MockMvcRequestBuilders.patch(url))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        boolean relation = territoryRepository.checkUserTerritoryRelation(savedUser.getId(), savedTerritory.getId());

        log.info("Check if attaching was ok");
        Assertions.assertThat(relation).isTrue();
    }

    @Test
    @SneakyThrows
    void detachUserFromTerritory_ifUserNotConnectedToTerritory_handleExceptionAndReturn409() {
        log.info("Saving user and territory");
        Territory territory = TestUtils.getTerritory();
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        String url = UrlConstants.TERR_ATTACH_DETACH_URL
                .formatted(savedTerritory.getId(), savedUser.getId());
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.DELETE, url);
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith("User")));
    }

    @Test
    @SneakyThrows
    void detachUserToTerritory_ifUserConnectedToTerritory_detachAndReturnNoContent() {
        log.info("Saving user and territory");
        Territory territory = TestUtils.getTerritory();
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        String url = UrlConstants.TERR_ATTACH_DETACH_URL
                .formatted(savedTerritory.getId(), savedUser.getId());
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.DELETE, url);
        mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        boolean relation = territoryRepository.checkUserTerritoryRelation(savedUser.getId(), savedTerritory.getId());

        log.info("Check if detaching was ok");
        Assertions.assertThat(relation).isFalse();
    }

}
