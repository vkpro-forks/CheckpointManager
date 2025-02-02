package ru.ac.checkpointmanager.it.controller;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.assertion.PassResultActionsAssert;
import ru.ac.checkpointmanager.config.EnablePostgresAndRedisTestContainers;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.extension.argprovider.PassUpdateDTOWithCarArgumentProvider;
import ru.ac.checkpointmanager.extension.argprovider.PassUpdateDTOWithVisitorArgumentProvider;
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
import ru.ac.checkpointmanager.util.CheckResultActionsUtils;
import ru.ac.checkpointmanager.util.MockMvcUtils;
import ru.ac.checkpointmanager.util.PassTestData;
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
@EnablePostgresAndRedisTestContainers
class PassControllerIntegrationTest {

    public static final String STATUS = "status";

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
    void addPass_NewCarWithoutIdWhenCarInRepoDoesntExists_SaveAndReturn() {
        saveTerritoryAndUser();
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithCar();
        passCreateDTO.setUserId(savedUser.getId());
        passCreateDTO.setTerritoryId(savedTerritory.getId());
        assert passCreateDTO.getCar() != null;
        passCreateDTO.getCar().setId(null);
        passCreateDTO.getCar().setBrand(TestUtils.getCarBrandDTO());//set saved car brand, if no car brand in DB, 404 will be thrown

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        resultActions.andExpect(MockMvcResultMatchers.status().isCreated())
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
    void addPass_NewCarWithoutIdWhenCarInRepoDoesntExistsAndBrandDoesntExists_SaveAndReturn() {
        saveTerritoryUserCarBrand();
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithCar();
        passCreateDTO.setUserId(savedUser.getId());
        passCreateDTO.setTerritoryId(savedTerritory.getId());
        assert passCreateDTO.getCar() != null;
        passCreateDTO.getCar().setId(null);
        passCreateDTO.getCar().setBrand(TestUtils.getCarBrandDTO());//set saved car brand, if no car brand in DB, 404 will be thrown

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        resultActions.andExpect(MockMvcResultMatchers.status().isCreated())
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
    void addPass_ExistingCarIdWhenCarInRepoExists_SaveAndReturn() {
        saveTerritoryUserCarBrand();
        saveCar();
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithCar();
        passCreateDTO.setUserId(savedUser.getId());
        passCreateDTO.setTerritoryId(savedTerritory.getId());
        assert passCreateDTO.getCar() != null;
        passCreateDTO.getCar().setBrand(TestUtils.getCarBrandDTO());//set saved car brand, if no car brand in DB, 404 will be thrown
        passCreateDTO.getCar().setId(savedCar.getId());
        passCreateDTO.getCar().setLicensePlate(savedCar.getLicensePlate());

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        resultActions.andExpect(MockMvcResultMatchers.status().isCreated())
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
    void addPass_ExistingCarInRepoButCarInDtoWillBeWithoutId_SaveAndReturn() {
        saveTerritoryUserCarBrand();
        saveCar();
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithCar();
        passCreateDTO.setUserId(savedUser.getId());
        passCreateDTO.setTerritoryId(savedTerritory.getId());
        assert passCreateDTO.getCar() != null;
        passCreateDTO.getCar().setBrand(TestUtils.getCarBrandDTO());//set saved car brand, if no car brand in DB, 404 will be thrown
        passCreateDTO.getCar().setId(null);//don't pass ID of car, pass only license plate
        passCreateDTO.getCar().setLicensePlate(savedCar.getLicensePlate());

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        resultActions.andExpect(MockMvcResultMatchers.status().isCreated())
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
    void addPass_NoCarInRepoButCarInDtoWillBeWithId_SaveAndReturn() {
        saveTerritoryUserCarBrand();
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithCar();
        passCreateDTO.setUserId(savedUser.getId());
        passCreateDTO.setTerritoryId(savedTerritory.getId());
        assert passCreateDTO.getCar() != null;
        passCreateDTO.getCar().setBrand(TestUtils.getCarBrandDTO());//set saved car brand, if no car brand in DB, 404 will be thrown
        passCreateDTO.getCar().setId(TestUtils.CAR_ID);
        passCreateDTO.getCar().setLicensePlate(TestUtils.LICENSE_PLATE);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        resultActions.andExpect(MockMvcResultMatchers.status().isCreated())
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

    @Test
    @SneakyThrows
    void addPass_VisitorWithoutIdNotInDb_SaveAndReturn() {
        saveTerritoryAndUser();
        PassCreateDTO passCreateDto = PassTestData.getPassCreateDTOWithVisitor();
        assert passCreateDto.getVisitor() != null;
        passCreateDto.getVisitor().setId(null);
        passCreateDto.setTerritoryId(savedTerritory.getId());
        passCreateDto.setUserId(savedUser.getId());

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDto));

        resultActions.andExpect(MockMvcResultMatchers.status().isCreated()).andExpectAll(
                MockMvcResultMatchers.jsonPath("$.id").isNotEmpty(),
                MockMvcResultMatchers.jsonPath("$.visitor.id").isNotEmpty());
        Assertions.assertThat(visitorRepository.findAll()).isNotEmpty().flatExtracting(Visitor::getName)
                .containsOnly(passCreateDto.getVisitor().getName());
        Assertions.assertThat(passRepository.findAll()).hasSize(1);
    }

    @Test
    @SneakyThrows
    void addPass_VisitorWithIdNotInDb_SaveAndReturn() {
        saveTerritoryAndUser();
        PassCreateDTO passCreateDto = PassTestData.getPassCreateDTOWithVisitor();
        assert passCreateDto.getVisitor() != null;
        passCreateDto.getVisitor().setId(TestUtils.VISITOR_ID);
        passCreateDto.setTerritoryId(savedTerritory.getId());
        passCreateDto.setUserId(savedUser.getId());

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDto));

        resultActions.andExpect(MockMvcResultMatchers.status().isCreated()).andExpectAll(
                MockMvcResultMatchers.jsonPath("$.id").isNotEmpty(),
                MockMvcResultMatchers.jsonPath("$.visitor.id").value(TestUtils.VISITOR_ID.toString()));
        Assertions.assertThat(visitorRepository.findAll()).isNotEmpty().flatExtracting(Visitor::getName)
                .containsOnly(passCreateDto.getVisitor().getName());
        Assertions.assertThat(passRepository.findAll()).hasSize(1);
    }

    @Test
    @SneakyThrows
    void addPass_VisitorWithIdInDB_SaveAndReturn() {
        saveTerritoryAndUser();
        Visitor visitorUnsaved = TestUtils.getVisitorRandomUUID();
        Visitor savedVisitor = visitorRepository.saveAndFlush(visitorUnsaved);
        PassCreateDTO passCreateDto = PassTestData.getPassCreateDTOWithVisitor();
        assert passCreateDto.getVisitor() != null;
        passCreateDto.getVisitor().setId(savedVisitor.getId());
        String updatedName = "Huggy Wuggy";
        passCreateDto.getVisitor().setName(updatedName);
        passCreateDto.setTerritoryId(savedTerritory.getId());
        passCreateDto.setUserId(savedUser.getId());

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDto));

        resultActions.andExpect(MockMvcResultMatchers.status().isCreated()).andExpectAll(
                MockMvcResultMatchers.jsonPath("$.id").isNotEmpty(),
                MockMvcResultMatchers.jsonPath("$.visitor.id").value(savedVisitor.getId().toString()));
        Assertions.assertThat(visitorRepository.findAll()).isNotEmpty().flatExtracting(Visitor::getName)
                .containsOnly(updatedName);
        Assertions.assertThat(passRepository.findAll()).hasSize(1);
    }

    @Test
    @SneakyThrows
    void addPass_NoUser_HandleUserNotFoundExceptionForAddPass() {
        PassCreateDTO passCreateDTOWithCar = PassTestData.getPassCreateDTOWithCar();

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTOWithCar));

        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(passCreateDTOWithCar.getUserId())));
        CheckResultActionsUtils.checkNotFoundFields(resultActions);
    }

    //UPDATE PASS

    @ParameterizedTest
    @ArgumentsSource(PassUpdateDTOWithCarArgumentProvider.class)
    @SneakyThrows
    void updatePass_AllOkShouldUpdateWithCars_ReturnUpdatedPass(PassUpdateDTO passUpdateDTO) {
        saveTerritoryUserCarBrandAndCar();
        PassAuto passAuto = passRepository.saveAndFlush(
                PassTestData.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar));
        passUpdateDTO.setId(passAuto.getId());
        ResultActions resultActions = mockMvc.perform(MockMvcUtils.updatePass(passUpdateDTO));
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());
        assert passUpdateDTO.getCar() != null;

        PassResultActionsAssert.assertThat(resultActions).commentMatches(passUpdateDTO.getComment())
                .startDateMatches(passUpdateDTO.getStartTime())
                .endDateMatches(passUpdateDTO.getEndTime())
                .passTimeTypeMatches(passUpdateDTO.getTimeType())
                .passCarLicensePlateMatches(passUpdateDTO.getCar().getLicensePlate())
                .contentTypeIsAppJson();
    }

    @ParameterizedTest
    @ArgumentsSource(PassUpdateDTOWithVisitorArgumentProvider.class)
    @SneakyThrows
    void updatePass_AllOkShouldUpdateWithVisitors_ReturnUpdatedPass(PassUpdateDTO passUpdateDTO) {
        saveTerritoryAndUser();
        Visitor visitorUnsaved = TestUtils.getVisitorRandomUUID();
        Visitor savedVisitor = visitorRepository.saveAndFlush(visitorUnsaved);
        PassWalk savedPassWalk = passRepository.saveAndFlush(
                PassTestData.getSimpleActiveOneTimePassWalkFor3Hours(savedUser, savedTerritory, savedVisitor));
        passUpdateDTO.setId(savedPassWalk.getId());
        VisitorDTO visitorDTO = passUpdateDTO.getVisitor();
        ResultActions resultActions = mockMvc.perform(MockMvcUtils.updatePass(passUpdateDTO));
        resultActions.andExpect(MockMvcResultMatchers.status().isOk());

        assert visitorDTO != null;
        PassResultActionsAssert.assertThat(resultActions).commentMatches(passUpdateDTO.getComment())
                .startDateMatches(passUpdateDTO.getStartTime())
                .endDateMatches(passUpdateDTO.getEndTime())
                .passTimeTypeMatches(passUpdateDTO.getTimeType())
                .passVisitorNameMatches(visitorDTO.getName())
                .passVisitorPhoneMatches(visitorDTO.getPhone())
                .passVisitorNoteMatches(visitorDTO.getNote())
                .contentTypeIsAppJson();
    }

    //DELETING PASSES
    @ParameterizedTest
    @EnumSource(PassStatus.class)
    @SneakyThrows
    void deletePass_PassWithAuto_ReturnNoContent(PassStatus passStatus) {
        saveTerritoryUserCarBrandAndCar();
        PassAuto pass = PassTestData.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar);
        pass.setStatus(passStatus);
        PassAuto savedPass = passRepository.saveAndFlush(pass);
        List<Pass> allPasses = passRepository.findAll();
        Assertions.assertThat(allPasses).as("Check if only one pass here").hasSize(1);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.deletePass(savedPass.getId()));

        resultActions.andExpect(MockMvcResultMatchers.status().isNoContent());

        List<Pass> passesAfterDelete = passRepository.findAll();
        Assertions.assertThat(passesAfterDelete).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(PassStatus.class)
    @SneakyThrows
    void deletePass_ManyPassesWithOneAuto_ReturnNoContent(PassStatus passStatus) {
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

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.deletePass(allPasses.get(0).getId()));

        resultActions.andExpect(MockMvcResultMatchers.status().isNoContent());

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
    void getPassesByUser_NoUser_HandleUserNotFoundExceptionAndReturnError() {
        ResultActions resultActions = mockMvc.perform(MockMvcUtils.getPassesByUserId(TestUtils.USER_ID));

        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(TestUtils.USER_ID)));
        CheckResultActionsUtils.checkNotFoundFields(resultActions);
    }

    @ParameterizedTest
    @EnumSource(PassStatus.class)
    @SneakyThrows
    void getPassesByUsersTerritories_FivePassesForUser_ReturnPassDTOs(PassStatus passStatus) {
        saveTerritoryUserCarBrand();
        saveCar();
        List<Pass> passes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PassAuto pass = createPassWithStatusAndTime(passStatus, i);
            passes.add(pass);
        }
        passRepository.saveAllAndFlush(passes);
        List<Pass> allPasses = passRepository.findAll();
        Assertions.assertThat(allPasses).hasSize(5);

        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.get(UrlConstants.PASS_USER_TERRITORIES_URL, savedUser.getId()));
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
                        .param(STATUS, PassStatus.ACTIVE.name()));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(3));
    }

    @Test
    @SneakyThrows
    void getPassesByUsersTerritories_FilteredByActive_ReturnPassDTOs() {
        saveTerritoryUserCarBrand();
        saveCar();
        List<Pass> passes = new ArrayList<>();
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
                .perform(MockMvcUtils.getPassesByUserId(savedUser.getId())
                        .param(STATUS, PassStatus.ACTIVE.name()));

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
                .perform(MockMvcUtils.getPassesByUserId(savedUser.getId()).param(STATUS, filterParams));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(totalFound));
    }

    @ParameterizedTest
    @MethodSource("getPassesForFilterByStatus")
    @SneakyThrows
    void getPassesByUsersTerritories_FilteredByActiveAndDelayed_ReturnPassDTOs(int total, int numIf, PassStatus statusIf,
                                                                               PassStatus statusElse, String filterParams,
                                                                               int totalFound) {
        saveTerritoryUserCarBrand();
        saveCar();
        List<Pass> passes = new ArrayList<>();
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
                .perform(MockMvcUtils.getPassesByUsersTerritories(savedUser.getId()).param(STATUS, filterParams));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(totalFound));
    }


    @Test
    @SneakyThrows
    void getPass_NotFound_ReturnNotFound() {
        ResultActions resultActions = mockMvc.perform(MockMvcUtils.getPass(PassTestData.PASS_ID));

        CheckResultActionsUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void updatePass_NotFound_ReturnNotFound() {
        ResultActions resultActions = mockMvc.perform(MockMvcUtils.updatePass(PassTestData.getPassUpdateDTOWithCar()));

        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.PASS_NOT_FOUND.formatted(PassTestData.PASS_ID)));
        CheckResultActionsUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void deletePass_NotFound_ReturnNotFound() {
        ResultActions resultActions = mockMvc.perform(MockMvcUtils.deletePass(PassTestData.PASS_ID));

        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.PASS_NOT_FOUND.formatted(PassTestData.PASS_ID)));
        CheckResultActionsUtils.checkNotFoundFields(resultActions);
    }

    @ParameterizedTest
    @MethodSource("passUrlsForPatchMethodsArguments")
    @SneakyThrows
    void patchMethods_NotFound_ReturnNotFound(String url) {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.patch(url, PassTestData.PASS_ID));

        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.PASS_NOT_FOUND.formatted(PassTestData.PASS_ID)));
        CheckResultActionsUtils.checkNotFoundFields(resultActions);
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
        saveTerritoryAndUser();
        CarBrand carBrand = TestUtils.getCarBrand();
        savedCarBrand = carBrandRepository.saveAndFlush(carBrand);
    }

    private void saveTerritoryAndUser() {
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        savedTerritory = territoryRepository.saveAndFlush(territory);
    }

    private void saveCar() {
        Car car = new Car();
        car.setLicensePlate(TestUtils.getCarDto().getLicensePlate());
        car.setBrand(savedCarBrand);
        car.setId(TestUtils.getCarDto().getId());
        savedCar = carRepository.saveAndFlush(car);//save car and repo change its id
    }

    private void saveTerritoryUserCarBrandAndCar() {
        saveTerritoryUserCarBrand();
        saveCar();
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

    private static Stream<String> passUrlsForPatchMethodsArguments() {
        return Stream.of(
                UrlConstants.PASS_URL_FAVORITE,
                UrlConstants.PASS_URL_UNWARNING,
                UrlConstants.PASS_URL_ACTIVATE,
                UrlConstants.PASS_URL_CANCEL,
                UrlConstants.PASS_URL_NOT_FAVORITE
        );
    }
}
