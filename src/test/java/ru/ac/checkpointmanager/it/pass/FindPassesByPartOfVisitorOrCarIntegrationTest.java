package ru.ac.checkpointmanager.it.pass;

import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.EnablePostgresAndRedisTestContainers;
import ru.ac.checkpointmanager.config.annotation.MockMvcIntegrationTest;
import ru.ac.checkpointmanager.extension.annotation.InjectSavedEntitiesForPassTest;
import ru.ac.checkpointmanager.extension.pass.CarVisitorUserTerritoryDto;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.model.passes.PassTimeType;
import ru.ac.checkpointmanager.model.passes.PassWalk;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.repository.VisitorRepository;
import ru.ac.checkpointmanager.repository.car.CarRepository;
import ru.ac.checkpointmanager.util.MockMvcUtils;
import ru.ac.checkpointmanager.util.PassTestData;
import ru.ac.checkpointmanager.util.TestUtils;

import java.time.LocalDateTime;
import java.util.Collections;

@MockMvcIntegrationTest
@WithMockUser(roles = {"ADMIN"})
@EnablePostgresAndRedisTestContainers
class FindPassesByPartOfVisitorOrCarIntegrationTest {

    private static final String FILTER_PARAMS = String.join(",", PassStatus.ACTIVE.name(), PassStatus.DELAYED.name());
    private static final String STATUS = "status";
    private static final String PART = "part";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PassRepository passRepository;

    @Autowired
    VisitorRepository visitorRepository;

    @Autowired
    CarRepository carRepository;

    CarVisitorUserTerritoryDto carVisitorUserTerritoryDto;

    public FindPassesByPartOfVisitorOrCarIntegrationTest(@InjectSavedEntitiesForPassTest
                                                         CarVisitorUserTerritoryDto carVisitorUserTerritoryDto) {
        this.carVisitorUserTerritoryDto = carVisitorUserTerritoryDto;
    }


    @AfterEach
    void clear() {
        visitorRepository.deleteAll();
        carRepository.deleteAll();
        passRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void getPassesByUserId_WithPartOfVisitorNameAndCarNumber_ReturnListWithPassesWithCarPass() {
        Pair<PassAuto, PassWalk> passes = savePassAutoAndPassWalkWithLicensePlateAndVisitorName(TestUtils.getCarDto().getLicensePlate(),
                "Vasya");

        ResultActions resultActions = mockMvc
                .perform(MockMvcUtils.getPassesByUserId(carVisitorUserTerritoryDto.getUser().getId())
                        .param(STATUS, FILTER_PARAMS)
                        .param(PART, "А")); //cyrillic A

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(passes.getLeft().getId().toString()));
    }

    @Test
    @SneakyThrows
    void getPassesByTerritoryId_WithPartOfVisitorNameAndCarNumber_ReturnListWithPassesWithCarPass() {
        Pair<PassAuto, PassWalk> passes = savePassAutoAndPassWalkWithLicensePlateAndVisitorName(TestUtils.getCarDto().getLicensePlate(),
                "Vasya");
        ResultActions resultActions = mockMvc
                .perform(MockMvcUtils.getPassesByTerritoryId(carVisitorUserTerritoryDto.getTerritory().getId())
                        .param(STATUS, FILTER_PARAMS)
                        .param(PART, "А"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(passes.getLeft().getId().toString()));
    }

    @Test
    @SneakyThrows
    void getPassesByUsersTerritories_WithPartOfVisitorNameAndCarNumber_ReturnListWithPassesWithCarPass() {
        Pair<PassAuto, PassWalk> passes = savePassAutoAndPassWalkWithLicensePlateAndVisitorName(TestUtils.getCarDto().getLicensePlate(),
                "Vasya");

        ResultActions resultActions = mockMvc
                .perform(MockMvcUtils.getPassesByUsersTerritories(carVisitorUserTerritoryDto.getUser().getId())
                        .param(STATUS, FILTER_PARAMS)
                        .param(PART, "А"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(passes.getLeft().getId().toString()));
    }

    @Test
    @SneakyThrows
    void getPasses_WithPartOfVisitorNameAndCarNumber_ReturnListWithPassesWithCarPass() {
        Pair<PassAuto, PassWalk> passes = savePassAutoAndPassWalkWithLicensePlateAndVisitorName(TestUtils.getCarDto().getLicensePlate(),
                "Vasya");

        ResultActions resultActions = mockMvc
                .perform(MockMvcUtils.getPasses()
                        .param(STATUS, FILTER_PARAMS)
                        .param(PART, "А"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(passes.getLeft().getId().toString()));
    }

    @Test
    @SneakyThrows
    void getPasses_WithPartOfVisitorNameAndCarNumberInTheMiddle_ReturnListWithPassesBothAutoAndWalk() {
        Pair<PassAuto, PassWalk> passes = savePassAutoAndPassWalkWithLicensePlateAndVisitorName(TestUtils.getCarDto().getLicensePlate(),
                "Вася");

        ResultActions resultActions = mockMvc
                .perform(MockMvcUtils.getPasses()
                        .param(STATUS, FILTER_PARAMS)
                        .param(PART, "А"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[*].id", Matchers.hasItems(
                        passes.getLeft().getId().toString(),
                        passes.getRight().getId().toString()
                )));
    }

    @Test
    @SneakyThrows
    void getPasses_WithPartOfVisitorNameAndCarNumberWithUpperAndLowerCase_ReturnListWithPassesBothAutoAndWalk() {
        Pair<PassAuto, PassWalk> passes = savePassAutoAndPassWalkWithLicensePlateAndVisitorName(TestUtils.getCarDto().getLicensePlate(),
                "ВАСЯ");
        ResultActions resultActions = mockMvc
                .perform(MockMvcUtils.getPasses()
                        .param(STATUS, FILTER_PARAMS)
                        .param(PART, "А"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[*].id", Matchers.hasItems(
                        passes.getLeft().getId().toString(),
                        passes.getRight().getId().toString()
                )));
    }

    @Test
    @SneakyThrows
    void getPasses_WithPartOfVisitorNameAndCarNumber_ReturnListWithPassesWalkOnly() {
        Pair<PassAuto, PassWalk> passes = savePassAutoAndPassWalkWithLicensePlateAndVisitorName(TestUtils.getCarDto().getLicensePlate(),
                "Petya");

        ResultActions resultActions = mockMvc
                .perform(MockMvcUtils.getPasses()
                        .param(STATUS, FILTER_PARAMS)
                        .param(PART, "ety"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id", Matchers.is(
                        passes.getRight().getId().toString())));
    }

    @Test
    @SneakyThrows
    void getPasses_WithPartOfVisitorNameAndCarNumberWithFilterActive_ReturnListWithPassesWalkOnly() {
        Car car = getCarWithLicensePlate(TestUtils.getCarDto().getLicensePlate());
        PassAuto passAuto = PassTestData.getSimpleActiveOneTimePassAutoFor3Hours(
                carVisitorUserTerritoryDto.getUser(), carVisitorUserTerritoryDto.getTerritory(), car
        );
        passAuto.setStatus(PassStatus.DELAYED);
        PassWalk passWalk = PassTestData.getPassWalk(PassStatus.ACTIVE, LocalDateTime.now(),
                LocalDateTime.now().plusDays(1), carVisitorUserTerritoryDto.getUser(),
                carVisitorUserTerritoryDto.getTerritory(), getVisitorWithName("Вася"), PassTimeType.PERMANENT);

        passRepository.saveAndFlush(passAuto);
        PassWalk savedPassWalk = passRepository.saveAndFlush(passWalk);
        String filterParams = String.join(",", PassStatus.ACTIVE.name());

        ResultActions resultActions = mockMvc
                .perform(MockMvcUtils.getPasses()
                        .param(STATUS, filterParams)
                        .param(PART, "A"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id", Matchers.is(
                        savedPassWalk.getId().toString())));
    }

    private Car getCarWithLicensePlate(String licensePlate) {
        return new Car(TestUtils.getCarDto().getId(), licensePlate,
                carVisitorUserTerritoryDto.getCarBrand(),
                Collections.emptyList(),
                TestUtils.PHONE_NUM);
    }

    private Visitor getVisitorWithName(String name) {
        return new Visitor(TestUtils.VISITOR_ID, name, TestUtils.PHONE_NUM, Collections.emptyList(), "note");
    }

    private Pair<PassAuto, PassWalk> savePassAutoAndPassWalkWithLicensePlateAndVisitorName(String licensePlate,
                                                                                           String name) {
        Car car = getCarWithLicensePlate(licensePlate); //LICENSE_PLATE = "А420ВХ799"; cyrillic A
        PassAuto passAuto = PassTestData.getSimpleActiveOneTimePassAutoFor3Hours(
                carVisitorUserTerritoryDto.getUser(), carVisitorUserTerritoryDto.getTerritory(), car
        );
        PassWalk passWalk = PassTestData.getPassWalk(PassStatus.ACTIVE, LocalDateTime.now(),
                LocalDateTime.now().plusDays(1), carVisitorUserTerritoryDto.getUser(),
                carVisitorUserTerritoryDto.getTerritory(), getVisitorWithName(name), PassTimeType.PERMANENT);
        return Pair.of(passRepository.saveAndFlush(passAuto), passRepository.saveAndFlush(passWalk));
    }
}
