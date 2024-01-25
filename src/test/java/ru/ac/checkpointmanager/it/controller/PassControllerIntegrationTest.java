package ru.ac.checkpointmanager.it.controller;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.RedisAndPostgresTestContainersConfiguration;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.model.passes.PassTimeType;
import ru.ac.checkpointmanager.model.passes.PassWalk;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.repository.VisitorRepository;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.repository.car.CarRepository;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = {"ADMIN"})
class PassControllerIntegrationTest extends RedisAndPostgresTestContainersConfiguration {

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
    VisitorRepository visitorRepository;

    Territory savedTerritory;

    User savedUser;

    CarBrand savedCarBrand;

    Car savedCar;

    Visitor savedVisitor;

    @AfterEach
    void clear() {
        passRepository.deleteAll();
        carRepository.deleteAll();
        carBrandRepository.deleteAll();
        visitorRepository.deleteAll();
        territoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    //ADDING PASSES
    @Test
    @SneakyThrows
    void shouldAddPassWithNewCarWithoutIdWhenCarInRepoDoesntExists() {
        saveTerritoryUserCarBrand();
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        passCreateDTO.setUserId(savedUser.getId());
        passCreateDTO.setTerritoryId(savedTerritory.getId());
        passCreateDTO.getCar().setId(null);
        passCreateDTO.getCar().setBrand(TestUtils.getCarBrandDTO());//set saved car brand, if no car brand in DB, 404 will be thrown
        String passCreateDtoString = TestUtils.jsonStringFromObject(passCreateDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passCreateDtoString))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id").value(savedUser.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.territory.id").value(savedTerritory.getId().toString()));
        List<Car> allCars = carRepository.findAll();
        Assertions.assertThat(allCars).hasSize(1);
        Car car = allCars.get(0);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.car.id").value(car.getId().toString()));
        List<Pass> allPasses = passRepository.findAll();
        Assertions.assertThat(allPasses).hasSize(1);
    }

    @Test
    @SneakyThrows
    void shouldAddPassWithExistingCarsIdWhenCarInRepoExists() {
        saveTerritoryUserCarBrand();
        saveCar();
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        passCreateDTO.setUserId(savedUser.getId());
        passCreateDTO.setTerritoryId(savedTerritory.getId());
        passCreateDTO.getCar().setBrand(TestUtils.getCarBrandDTO());//set saved car brand, if no car brand in DB, 404 will be thrown
        passCreateDTO.getCar().setId(savedCar.getId());
        passCreateDTO.getCar().setLicensePlate(savedCar.getLicensePlate());
        String passCreateDtoString = TestUtils.jsonStringFromObject(passCreateDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passCreateDtoString))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id").value(savedUser.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.territory.id").value(savedTerritory.getId().toString()));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.car.id").value(savedCar.getId().toString()));

        List<Car> allCars = carRepository.findAll();
        Assertions.assertThat(allCars).hasSize(1);//check if no added cars

        List<Pass> allPasses = passRepository.findAll();
        Assertions.assertThat(allPasses).hasSize(1);//check if only one pass here
    }

    @Test
    @SneakyThrows
    void shouldAddPassWithExistingCarInRepoButCarInDtoWillBeWithoutId() {
        saveTerritoryUserCarBrand();
        saveCar();
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        passCreateDTO.setUserId(savedUser.getId());
        passCreateDTO.setTerritoryId(savedTerritory.getId());
        passCreateDTO.getCar().setBrand(TestUtils.getCarBrandDTO());//set saved car brand, if no car brand in DB, 404 will be thrown
        passCreateDTO.getCar().setId(null);//don't pass ID of car, pass only license plate
        passCreateDTO.getCar().setLicensePlate(savedCar.getLicensePlate());
        String passCreateDtoString = TestUtils.jsonStringFromObject(passCreateDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passCreateDtoString))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id").value(savedUser.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.territory.id").value(savedTerritory.getId().toString()));

        org.junit.jupiter.api.Assertions.assertAll(
                () -> {
                    List<Car> carsByUserId = carRepository.findCarsByUserId(savedUser.getId());
                    Assertions.assertThat(carsByUserId).hasSize(1);
                    Car carInPass = carsByUserId.get(0);
                    resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.car.id")
                            .value(carInPass.getId().toString()));
                },
                () -> {
                    List<Car> allCars = carRepository.findAll();
                    Assertions.assertThat(allCars).as("Check if no added cars, 2 was before").hasSize(2);
                },
                () -> {
                    List<Pass> allPasses = passRepository.findAll();
                    Assertions.assertThat(allPasses).as("Check if only one pass here").hasSize(1);
                }
        );
    }

    @Test
    @SneakyThrows
    void shouldAddPassWithNoCarInRepoButCarInDtoWillBeWithId() {
        saveTerritoryUserCarBrand();
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        passCreateDTO.setUserId(savedUser.getId());
        passCreateDTO.setTerritoryId(savedTerritory.getId());
        passCreateDTO.getCar().setBrand(TestUtils.getCarBrandDTO());//set saved car brand, if no car brand in DB, 404 will be thrown
        passCreateDTO.getCar().setId(TestUtils.CAR_ID);
        passCreateDTO.getCar().setLicensePlate(TestUtils.LICENSE_PLATE);
        String passCreateDtoString = TestUtils.jsonStringFromObject(passCreateDTO);

        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passCreateDtoString))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id").value(savedUser.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.territory.id").value(savedTerritory.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.car.id").value(passCreateDTO.getCar().getId().toString()));

        org.junit.jupiter.api.Assertions.assertAll(
                () -> {
                    List<Car> allCars = carRepository.findAll();
                    Assertions.assertThat(allCars).as("Check if no added cars").hasSize(1);
                },
                () -> {
                    List<Pass> allPasses = passRepository.findAll();
                    Assertions.assertThat(allPasses).as("Check if only one pass here").hasSize(1);
                }
        );
    }

    //DELETING PASSES
    @ParameterizedTest
    @EnumSource(PassStatus.class)
    @SneakyThrows
    void shouldDeletePassWithAuto(PassStatus passStatus) {
        saveTerritoryUserCarBrand();
        saveCar();
        PassAuto pass = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar);
        pass.setStatus(passStatus);
        PassAuto savedPass = passRepository.saveAndFlush(pass);
        List<Pass> allPasses = passRepository.findAll();
        Assertions.assertThat(allPasses).as("Check if only one pass here").hasSize(1);

        mockMvc.perform(MockMvcRequestBuilders.delete(UrlConstants.PASS_URL + "/" + savedPass.getId()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        List<Pass> passesAfterDelete = passRepository.findAll();
        Assertions.assertThat(passesAfterDelete).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(PassStatus.class)
    @SneakyThrows
    void shouldDeleteManyPassesWithOneAuto(PassStatus passStatus) {
        //creating a passes for territory with one car
        saveTerritoryUserCarBrand();
        saveCar();
        List<Pass> passes = new ArrayList<>();
        //created 5 passes for one car
        for (int i = 0; i < 5; i++) {
            PassAuto pass = createPassWithStatusAndTime(passStatus, i);
            passes.add(pass);
        }
        passRepository.saveAllAndFlush(passes);
        List<Pass> allPasses = passRepository.findAll();
        Assertions.assertThat(allPasses).hasSize(5);

        mockMvc.perform(MockMvcRequestBuilders.delete(UrlConstants.PASS_URL + "/" + allPasses.get(0).getId()))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        List<Pass> passesAfterDelete = passRepository.findAll();
        Assertions.assertThat(passesAfterDelete).hasSize(4);
    }

    //GETTING PASSES
    @ParameterizedTest
    @EnumSource(PassStatus.class)
    @SneakyThrows
    void getPasses_FivePassesForUser_ReturnPassDTOs(PassStatus passStatus) {
        //creating a passes for territory with one car
        saveTerritoryUserCarBrand();
        saveCar();
        List<Pass> passes = new ArrayList<>();
        //created 5 passes for one car
        for (int i = 0; i < 5; i++) {
            PassAuto pass = createPassWithStatusAndTime(passStatus, i);
            passes.add(pass);
        }
        passRepository.saveAllAndFlush(passes);
        List<Pass> allPasses = passRepository.findAll();
        Assertions.assertThat(allPasses).hasSize(5);

        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.get(UrlConstants.PASS_USER_URL, savedUser.getId()));
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(5));
    }

    @Test
    @SneakyThrows
    void getPasses_FilteredByActive_ReturnPassDTOs() {
        saveTerritoryUserCarBrand();
        saveCar();
        List<Pass> passes = new ArrayList<>();
        //created 5 passes for one car
        PassStatus passStatus;
        for (int i = 0; i < 5; i++) {
            if (i < 3) {
                passStatus = PassStatus.ACTIVE;
            } else {
                passStatus = PassStatus.DELAYED;
            }
            PassAuto pass = createPassWithStatusAndTime(passStatus, i);
            passes.add(pass);
        }
        passRepository.saveAllAndFlush(passes);

        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.get(UrlConstants.PASS_USER_URL, savedUser.getId())
                        .param("status", PassStatus.ACTIVE.name()));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(3));
    }

    @ParameterizedTest
    @MethodSource("getPassesForFilterByStatus")
    @SneakyThrows
    void getPasses_FilteredByActiveAndDelayed_ReturnPassDTOs(int total, int numIf, PassStatus statusIf,
                                                             PassStatus statusElse, String filterParams,
                                                             int totalFound) {
        saveTerritoryUserCarBrand();
        saveCar();
        List<Pass> passes = new ArrayList<>();
        //created 5 passes for one car
        PassStatus passStatus;
        for (int i = 0; i < total; i++) {
            if (i < numIf) {
                passStatus = statusIf;
            } else {
                passStatus = statusElse;
            }
            PassAuto pass = createPassWithStatusAndTime(passStatus, i);
            passes.add(pass);
        }
        passRepository.saveAllAndFlush(passes);

        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.get(UrlConstants.PASS_USER_URL, savedUser.getId())
                        .param("status", filterParams));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(totalFound));
    }

    @Test
    @SneakyThrows
    void getPassesByPartOfVisitorNameAndCarNumber_AllOk_ReturnListWithPassesWithCarPass() {
        saveTerritoryUserCarBrand();
        saveCar(); //LICENSE_PLATE = "А420ВХ799";
        PassAuto passAuto = createPassWithStatusAndTime(PassStatus.ACTIVE, 5);
        Visitor visitor = new Visitor();
        visitor.setName("Vasya");
        visitor.setPhone(TestUtils.PHONE_NUM);
        PassWalk passWalk = TestUtils.getPassWalk(PassStatus.ACTIVE, LocalDateTime.now(),
                LocalDateTime.now().plusDays(1), savedUser,
                savedTerritory, visitor, PassTimeType.PERMANENT);
        PassAuto savedPassAuto = passRepository.saveAndFlush(passAuto);
        PassWalk savedPassWalk = passRepository.saveAndFlush(passWalk);
        String filterParams = String.join(",", PassStatus.ACTIVE.name(), PassStatus.DELAYED.name());

        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.get(UrlConstants.PASS_URL_SEARCH)
                        .param("status", filterParams)
                        .param("part", "А"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(savedPassAuto.getId().toString()));
    }

    private PassAuto createPassWithStatusAndTime(PassStatus passStatus, int i) {
        PassAuto pass = new PassAuto();
        pass.setId(UUID.randomUUID());
        pass.setTimeType(PassTimeType.ONETIME);
        pass.setStartTime(LocalDateTime.now().plusHours(i));
        pass.setEndTime(LocalDateTime.now().plusHours(5 + i));
        pass.setStatus(passStatus);
        pass.setTerritory(savedTerritory);
        pass.setUser(savedUser);
        pass.setCar(savedCar);
        return pass;
    }

    private void saveTerritoryUserCarBrand() {
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        savedTerritory = territoryRepository.saveAndFlush(territory);
        CarBrand carBrand = TestUtils.getCarBrand();
        savedCarBrand = carBrandRepository.saveAndFlush(carBrand);
    }

    private void saveCar() {
        Car car = new Car();
        car.setLicensePlate(TestUtils.getCarDto().getLicensePlate());
        car.setBrand(savedCarBrand);
        car.setId(TestUtils.getCarDto().getId());
        savedCar = carRepository.saveAndFlush(car);//save car and repo change its id
    }

    private static Stream<Arguments> getPassesForFilterByStatus() {
        //total, first, statusIf, statusElse, filterParams, total found
        return Stream.of(
                Arguments.of(
                        5, 3, PassStatus.ACTIVE, PassStatus.DELAYED,
                        String.join(",", PassStatus.ACTIVE.name(), PassStatus.DELAYED.name()),
                        5
                ),
                Arguments.of(
                        5, 3, PassStatus.ACTIVE, PassStatus.DELAYED,
                        PassStatus.ACTIVE.name(), 3
                ),
                Arguments.of(
                        10, 3, PassStatus.WARNING, PassStatus.OUTDATED,
                        PassStatus.ACTIVE.name(), 0
                ),
                Arguments.of(
                        10, 3, PassStatus.WARNING, PassStatus.OUTDATED,
                        String.join(",", PassStatus.ACTIVE.name(), PassStatus.DELAYED.name(),
                                PassStatus.COMPLETED.name()), 0
                ),
                Arguments.of(
                        10, 3, PassStatus.WARNING, PassStatus.OUTDATED,
                        String.join(",", PassStatus.ACTIVE.name(), PassStatus.DELAYED.name(),
                                PassStatus.COMPLETED.name(), PassStatus.WARNING.name()), 3
                )
        );
    }

}
