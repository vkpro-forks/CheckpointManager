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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.EnablePostgresAndRedisTestContainers;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassWalk;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.repository.VisitorRepository;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.repository.car.CarRepository;
import ru.ac.checkpointmanager.util.MockMvcUtils;
import ru.ac.checkpointmanager.util.PassTestData;
import ru.ac.checkpointmanager.util.CheckResultActionsUtils;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = {"ADMIN"})
@Slf4j
@EnablePostgresAndRedisTestContainers
class VisitorControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    VisitorRepository visitorRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TerritoryRepository territoryRepository;

    @Autowired
    PassRepository passRepository;

    @Autowired
    CarRepository carRepository;

    @Autowired
    CarBrandRepository carBrandRepository;

    @AfterEach
    void clear() {
        visitorRepository.deleteAll();
        passRepository.deleteAll();
        carRepository.deleteAll();
        carBrandRepository.deleteAll();
        userRepository.deleteAll();
        territoryRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void addVisitor_AllOk_CreateAndReturnVisitor() {
        VisitorDTO visitorDTO = TestUtils.getVisitorDTO();

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createVisitor(visitorDTO));

        resultActions.andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(visitorDTO.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value(visitorDTO.getPhone()));
    }

    @Test
    @SneakyThrows
    void addVisitor_VisitorWithoutId_CreateAndReturnVisitor() {
        VisitorDTO visitorDTO = TestUtils.getVisitorDTO();
        visitorDTO.setId(null);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createVisitor(visitorDTO));

        resultActions.andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(visitorDTO.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value(visitorDTO.getPhone()));
    }

    @Test
    @SneakyThrows
    void getVisitor_AllOk_ReturnVisitor() {
        Visitor visitor = TestUtils.getVisitorRandomUUID();
        Visitor savedVisitor = visitorRepository.saveAndFlush(visitor);
        UUID visitorId = savedVisitor.getId();

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.getVisitor(visitorId));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(visitorId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(savedVisitor.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value(savedVisitor.getPhone()));
    }

    @Test
    @SneakyThrows
    void getVisitor_VisitorNotFound_HandleErrorAndReturnNotFound() {
        ResultActions resultActions = mockMvc.perform(MockMvcUtils.getVisitor(TestUtils.VISITOR_ID));

        CheckResultActionsUtils.checkNotFoundFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.VISITOR_NOT_FOUND.formatted(TestUtils.VISITOR_ID)));
    }

    @Test
    @SneakyThrows
    void updateVisitor_AllOk_ReturnVisitor() {
        Visitor visitor = TestUtils.getVisitorRandomUUID();
        Visitor savedVisitor = visitorRepository.saveAndFlush(visitor);
        UUID visitorId = savedVisitor.getId();
        VisitorDTO visitorForUpdate = TestUtils.getVisitorDTO();
        visitorForUpdate.setName("Huggy Wuggy");

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.updateVisitor(visitorForUpdate, visitorId));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(visitorId.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Huggy Wuggy"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.phone").value(savedVisitor.getPhone()));
    }

    @Test
    @SneakyThrows
    void updateVisitor_VisitorNotFound_HandleErrorAndReturnNotFound() {
        ResultActions resultActions = mockMvc.perform(
                MockMvcUtils.updateVisitor(TestUtils.getVisitorDTO(), TestUtils.VISITOR_ID));

        CheckResultActionsUtils.checkNotFoundFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.VISITOR_NOT_FOUND.formatted(TestUtils.VISITOR_ID)));
    }

    @Test
    @SneakyThrows
    void deleteVisitor_AllOk_DeleteAndReturnNoContent() {
        Visitor visitor = TestUtils.getVisitorRandomUUID();
        Visitor savedVisitor = visitorRepository.saveAndFlush(visitor);
        UUID visitorId = savedVisitor.getId();

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.deleteVisitor(visitorId));

        resultActions.andExpect(MockMvcResultMatchers.status().isNoContent());
        Optional<Visitor> optionalVisitor = visitorRepository.findById(visitorId);
        Assertions.assertThat(optionalVisitor).as("Check if visitor was not in repo").isEmpty();
    }

    @Test
    @SneakyThrows
    void deleteVisitor_VisitorNotFound_HandleErrorAndReturnNotFound() {
        ResultActions resultActions = mockMvc.perform(MockMvcUtils.deleteVisitor(TestUtils.VISITOR_ID));

        CheckResultActionsUtils.checkNotFoundFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.VISITOR_NOT_FOUND.formatted(TestUtils.VISITOR_ID)));
    }

    @Test
    @SneakyThrows
    void searchByPhonePart_IfTwoMatches_ReturnListOfVisitors() {
        Visitor visitorOneInList = TestUtils.getVisitorRandomUUID();
        visitorRepository.save(visitorOneInList);
        Visitor visitorShouldNotBeInList = TestUtils.getVisitorRandomUUID();
        visitorShouldNotBeInList.setPhone("+123 123 22 33");
        visitorRepository.save(visitorShouldNotBeInList);
        Visitor visitorTwoInList = TestUtils.getVisitorRandomUUID();
        visitorTwoInList.setPhone("+79167868122");
        visitorRepository.save(visitorTwoInList);
        visitorRepository.flush();

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.getVisitorByPhonePart().param("phone", "+7916"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].phone",
                        Matchers.hasItems(visitorOneInList.getPhone(), visitorTwoInList.getPhone())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].phone",
                        Matchers.not(Matchers.hasItem(visitorShouldNotBeInList.getPhone()))));
    }

    @Test
    @SneakyThrows
    void searchByPhonePart_IfNoMatches_ReturnEmptyList() {
        Visitor visitorShouldNotBeInList = TestUtils.getVisitorRandomUUID();
        visitorShouldNotBeInList.setPhone("+123 123 22 33");
        visitorRepository.saveAndFlush(visitorShouldNotBeInList);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.getVisitorByPhonePart().param("phone", "+7916"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0));
    }

    @Test
    @SneakyThrows
    void searchByNamePart_IfTwoMatches_ReturnListOfVisitors() {
        Visitor visitorOneInList = TestUtils.getVisitorRandomUUID();
        visitorRepository.save(visitorOneInList);
        Visitor visitorShouldNotBeInList = TestUtils.getVisitorRandomUUID();
        visitorShouldNotBeInList.setName("Bubuka");
        visitorRepository.save(visitorShouldNotBeInList);
        Visitor visitorTwoInList = TestUtils.getVisitorRandomUUID();
        visitorTwoInList.setName("UserPon");
        visitorRepository.save(visitorTwoInList);
        visitorRepository.flush();

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.getVisitorByNamePart().param("name", "User"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].name",
                        Matchers.hasItems(visitorOneInList.getName(), visitorTwoInList.getName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].phone",
                        Matchers.not(Matchers.hasItem(visitorShouldNotBeInList.getName()))));
    }

    @Test
    @SneakyThrows
    void searchByNamePart_IfNoMatches_ReturnListEmptyList() {
        Visitor visitorShouldNotBeInList = TestUtils.getVisitorRandomUUID();
        visitorShouldNotBeInList.setName("Bubuka");
        visitorRepository.saveAndFlush(visitorShouldNotBeInList);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.getVisitorByNamePart().param("name", "User"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0));
    }

    @Test
    @SneakyThrows
    void searchByPassId_AllOk_ReturnVisitor() {
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        Visitor visitorUnsaved = TestUtils.getVisitorRandomUUID();
        Visitor savedVisitor = visitorRepository.saveAndFlush(visitorUnsaved);
        PassWalk passWalk = PassTestData.getSimpleActiveOneTimePassWalkFor3Hours(savedUser, savedTerritory, savedVisitor);
        PassWalk savedPass = passRepository.saveAndFlush(passWalk);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.getVisitorByPassId(savedPass.getId()));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(savedVisitor.getId().toString()));
    }

    @Test
    @SneakyThrows
    void searchByPassId_IfAskForPassAuto_HandleErrorAndReturnNotFound() {
        //given
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        CarBrand carBrand = TestUtils.getCarBrand();
        CarBrand savedCarBrand = carBrandRepository.saveAndFlush(carBrand);
        Car car = new Car();
        car.setLicensePlate(TestUtils.getCarDto().getLicensePlate());
        car.setBrand(savedCarBrand);
        car.setId(TestUtils.getCarDto().getId());
        Car savedCar = carRepository.saveAndFlush(car);
        PassAuto pass = PassTestData.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar);
        PassAuto savedPass = passRepository.saveAndFlush(pass);
        //when
        ResultActions resultActions = mockMvc.perform(MockMvcUtils.getVisitorByPassId(savedPass.getId()));
        //then
        resultActions.andExpect(MockMvcResultMatchers.status().isNotFound());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.VISITOR_BY_PASS_NOT_FOUND.formatted(savedPass.getId())));
        CheckResultActionsUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void searchByPassId_IfPassNotExists_HandleAndReturnNotFound() {
        Visitor visitorUnsaved = TestUtils.getVisitorRandomUUID();
        visitorRepository.saveAndFlush(visitorUnsaved);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.getVisitorByPassId(PassTestData.PASS_ID));

        resultActions.andExpect(MockMvcResultMatchers.status().isNotFound());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.VISITOR_BY_PASS_NOT_FOUND.formatted(PassTestData.PASS_ID)));
        CheckResultActionsUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void searchByUserId_UserHasOnePassWithVisitor_ReturnListOfVisitors() {
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        Visitor visitorUnsaved = TestUtils.getVisitorRandomUUID();
        Visitor savedVisitor = visitorRepository.saveAndFlush(visitorUnsaved);
        PassWalk passWalk = PassTestData.getSimpleActiveOneTimePassWalkFor3Hours(savedUser, savedTerritory, savedVisitor);
        passRepository.saveAndFlush(passWalk);
        Visitor anotherVisitor = TestUtils.getVisitorRandomUUID();
        anotherVisitor.setName("Huggy Wuggy");
        visitorRepository.saveAndFlush(visitorUnsaved);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.getVisitorByUserId(savedUser.getId()));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[*].name", Matchers.hasItem(savedVisitor.getName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1));
    }

    @Test
    @SneakyThrows
    void searchByUserId_UserDoesntHavePassWithVisitorsHasOnlyPassWIthCar_ReturnEmptyList() {
        //given
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        CarBrand carBrand = TestUtils.getCarBrand();
        CarBrand savedCarBrand = carBrandRepository.saveAndFlush(carBrand);
        Car car = new Car();
        car.setLicensePlate(TestUtils.getCarDto().getLicensePlate());
        car.setBrand(savedCarBrand);
        car.setId(TestUtils.getCarDto().getId());
        Car savedCar = carRepository.saveAndFlush(car);
        PassAuto pass = PassTestData.getSimpleActiveOneTimePassAutoFor3Hours(savedUser, savedTerritory, savedCar);
        passRepository.saveAndFlush(pass);
        Visitor anotherVisitor = TestUtils.getVisitorRandomUUID();
        anotherVisitor.setName("Huggy Wuggy");
        visitorRepository.saveAndFlush(anotherVisitor);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.getVisitorByUserId(savedUser.getId()));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[*].name",
                        Matchers.not(Matchers.hasItem(anotherVisitor.getName()))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0));
    }

    @Test
    @SneakyThrows
    void searchByUserId_NoUserWithThisId_ReturnEmptyList() {
        Visitor anotherVisitor = TestUtils.getVisitorRandomUUID();
        anotherVisitor.setName("Huggy Wuggy");
        visitorRepository.saveAndFlush(anotherVisitor);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.getVisitorByUserId(TestUtils.USER_ID));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[*].name",
                        Matchers.not(Matchers.hasItem(anotherVisitor.getName()))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(0));
    }
}
