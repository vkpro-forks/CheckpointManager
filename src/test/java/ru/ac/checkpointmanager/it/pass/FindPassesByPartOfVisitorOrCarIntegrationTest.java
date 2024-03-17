package ru.ac.checkpointmanager.it.pass;

import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.EnablePostgresAndRedisTestContainers;
import ru.ac.checkpointmanager.config.annotation.MockMvcIntegrationTest;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.model.passes.PassTimeType;
import ru.ac.checkpointmanager.model.passes.PassWalk;
import ru.ac.checkpointmanager.util.MockMvcUtils;
import ru.ac.checkpointmanager.util.PassTestData;
import ru.ac.checkpointmanager.util.TestUtils;

import java.time.LocalDateTime;
@MockMvcIntegrationTest
@WithMockUser(roles = {"ADMIN"})
@EnablePostgresAndRedisTestContainers
class FindPassesByPartOfVisitorOrCarIntegrationTest {

    @Test
    @SneakyThrows
    void getPassesByUserId_WithPartOfVisitorNameAndCarNumber_ReturnListWithPassesWithCarPass() {
        saveTerritoryUserCarBrand();
        saveCar(); //LICENSE_PLATE = "А420ВХ799";
        PassAuto passAuto = createPassWithStatusAndTime(PassStatus.ACTIVE, 5);
        Visitor visitor = new Visitor();
        visitor.setId(TestUtils.VISITOR_ID);
        visitor.setName("Vasya");
        visitor.setPhone(TestUtils.PHONE_NUM);
        PassWalk passWalk = PassTestData.getPassWalk(PassStatus.ACTIVE, LocalDateTime.now(),
                LocalDateTime.now().plusDays(1), savedUser,
                savedTerritory, visitor, PassTimeType.PERMANENT);
        PassAuto savedPassAuto = passRepository.saveAndFlush(passAuto);
        passRepository.saveAndFlush(passWalk);
        String filterParams = String.join(",", PassStatus.ACTIVE.name(), PassStatus.DELAYED.name());

        ResultActions resultActions = mockMvc
                .perform(MockMvcUtils.getPassesByUserId(savedUser.getId())
                        .param(STATUS, filterParams)
                        .param("part", "А"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(savedPassAuto.getId().toString()));
    }

    @Test
    @SneakyThrows
    void getPassesByTerritoryId_WithPartOfVisitorNameAndCarNumber_ReturnListWithPassesWithCarPass() {
        saveTerritoryUserCarBrand();
        saveCar(); //LICENSE_PLATE = "А420ВХ799";
        PassAuto passAuto = createPassWithStatusAndTime(PassStatus.ACTIVE, 5);
        Visitor visitor = new Visitor();
        visitor.setId(TestUtils.VISITOR_ID);
        visitor.setName("Vasya");
        visitor.setPhone(TestUtils.PHONE_NUM);
        PassWalk passWalk = PassTestData.getPassWalk(PassStatus.ACTIVE, LocalDateTime.now(),
                LocalDateTime.now().plusDays(1), savedUser,
                savedTerritory, visitor, PassTimeType.PERMANENT);
        PassAuto savedPassAuto = passRepository.saveAndFlush(passAuto);
        passRepository.saveAndFlush(passWalk);
        String filterParams = String.join(",", PassStatus.ACTIVE.name(), PassStatus.DELAYED.name());

        ResultActions resultActions = mockMvc
                .perform(MockMvcUtils.getPassesByTerritoryId(savedTerritory.getId())
                        .param(STATUS, filterParams)
                        .param("part", "А"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(savedPassAuto.getId().toString()));
    }

    @Test
    @SneakyThrows
    void getPassesByUsersTerritories_WithPartOfVisitorNameAndCarNumber_ReturnListWithPassesWithCarPass() {
        saveTerritoryUserCarBrand();
        saveCar(); //LICENSE_PLATE = "А420ВХ799";
        PassAuto passAuto = createPassWithStatusAndTime(PassStatus.ACTIVE, 5);
        Visitor visitor = new Visitor();
        visitor.setId(TestUtils.VISITOR_ID);
        visitor.setName("Vasya");
        visitor.setPhone(TestUtils.PHONE_NUM);
        PassWalk passWalk = PassTestData.getPassWalk(PassStatus.ACTIVE, LocalDateTime.now(),
                LocalDateTime.now().plusDays(1), savedUser,
                savedTerritory, visitor, PassTimeType.PERMANENT);
        PassAuto savedPassAuto = passRepository.saveAndFlush(passAuto);
        passRepository.saveAndFlush(passWalk);
        String filterParams = String.join(",", PassStatus.ACTIVE.name(), PassStatus.DELAYED.name());

        ResultActions resultActions = mockMvc
                .perform(MockMvcUtils.getPassesByUsersTerritories(savedUser.getId())
                        .param(STATUS, filterParams)
                        .param("part", "А"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(savedPassAuto.getId().toString()));
    }

    @Test
    @SneakyThrows
    void getPasses_WithPartOfVisitorNameAndCarNumber_ReturnListWithPassesWithCarPass() {
        saveTerritoryUserCarBrand();
        saveCar(); //LICENSE_PLATE = "А420ВХ799";
        PassAuto passAuto = createPassWithStatusAndTime(PassStatus.ACTIVE, 5);
        Visitor visitor = new Visitor();
        visitor.setId(TestUtils.VISITOR_ID);
        visitor.setName("Vasya");
        visitor.setPhone(TestUtils.PHONE_NUM);
        PassWalk passWalk = PassTestData.getPassWalk(PassStatus.ACTIVE, LocalDateTime.now(),
                LocalDateTime.now().plusDays(1), savedUser,
                savedTerritory, visitor, PassTimeType.PERMANENT);
        PassAuto savedPassAuto = passRepository.saveAndFlush(passAuto);
        passRepository.saveAndFlush(passWalk);
        String filterParams = String.join(",", PassStatus.ACTIVE.name(), PassStatus.DELAYED.name());

        ResultActions resultActions = mockMvc
                .perform(MockMvcUtils.getPasses()
                        .param(STATUS, filterParams)
                        .param("part", "А"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(savedPassAuto.getId().toString()));
    }

    @Test
    @SneakyThrows
    void getPasses_WithPartOfVisitorNameAndCarNumber_ReturnListWithPassesBothAutoAndWalk() {
        saveTerritoryUserCarBrand();
        saveCar(); //LICENSE_PLATE = "А420ВХ799";
        PassAuto passAuto = createPassWithStatusAndTime(PassStatus.ACTIVE, 5);
        Visitor visitor = new Visitor();
        visitor.setId(TestUtils.VISITOR_ID);
        visitor.setName("А420");
        visitor.setPhone(TestUtils.PHONE_NUM);
        PassWalk passWalk = PassTestData.getPassWalk(PassStatus.ACTIVE, LocalDateTime.now(),
                LocalDateTime.now().plusDays(1), savedUser,
                savedTerritory, visitor, PassTimeType.PERMANENT);
        PassAuto savedPassAuto = passRepository.saveAndFlush(passAuto);
        PassWalk savedPassWalk = passRepository.saveAndFlush(passWalk);
        String filterParams = String.join(",", PassStatus.ACTIVE.name(), PassStatus.DELAYED.name());

        ResultActions resultActions = mockMvc
                .perform(MockMvcUtils.getPasses()
                        .param(STATUS, filterParams)
                        .param("part", "А"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[*].id", Matchers.hasItems(
                        savedPassAuto.getId().toString(),
                        savedPassWalk.getId().toString()
                )));
    }

    @Test
    @SneakyThrows
    void getPasses_WithPartOfVisitorNameAndCarNumberWithUpperAndLowerCase_ReturnListWithPassesBothAutoAndWalk() {
        saveTerritoryUserCarBrand();
        saveCar(); //LICENSE_PLATE = "А420ВХ799";
        PassAuto passAuto = createPassWithStatusAndTime(PassStatus.ACTIVE, 5);
        Visitor visitor = new Visitor();
        visitor.setId(TestUtils.VISITOR_ID);
        visitor.setName("а420");
        visitor.setPhone(TestUtils.PHONE_NUM);
        PassWalk passWalk = PassTestData.getPassWalk(PassStatus.ACTIVE, LocalDateTime.now(),
                LocalDateTime.now().plusDays(1), savedUser,
                savedTerritory, visitor, PassTimeType.PERMANENT);
        PassAuto savedPassAuto = passRepository.saveAndFlush(passAuto);
        PassWalk savedPassWalk = passRepository.saveAndFlush(passWalk);
        String filterParams = String.join(",", PassStatus.ACTIVE.name(), PassStatus.DELAYED.name());

        ResultActions resultActions = mockMvc
                .perform(MockMvcUtils.getPasses()
                        .param(STATUS, filterParams)
                        .param("part", "А"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[*].id", Matchers.hasItems(
                        savedPassAuto.getId().toString(),
                        savedPassWalk.getId().toString()
                )));
    }

    @Test
    @SneakyThrows
    void getPasses_WithPartOfVisitorNameAndCarNumber_ReturnListWithPassesWalkOnly() {
        saveTerritoryUserCarBrand();
        saveCar(); //LICENSE_PLATE = "А420ВХ799";
        PassAuto passAuto = createPassWithStatusAndTime(PassStatus.ACTIVE, 5);
        Visitor visitor = new Visitor();
        visitor.setId(TestUtils.VISITOR_ID);
        visitor.setName("Petya");
        visitor.setPhone(TestUtils.PHONE_NUM);
        PassWalk passWalk = PassTestData.getPassWalk(PassStatus.ACTIVE, LocalDateTime.now(),
                LocalDateTime.now().plusDays(1), savedUser,
                savedTerritory, visitor, PassTimeType.PERMANENT);
        passRepository.saveAndFlush(passAuto);
        PassWalk savedPassWalk = passRepository.saveAndFlush(passWalk);
        String filterParams = String.join(",", PassStatus.ACTIVE.name(), PassStatus.DELAYED.name());

        ResultActions resultActions = mockMvc
                .perform(MockMvcUtils.getPasses()
                        .param(STATUS, filterParams)
                        .param("part", "Pet"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id", Matchers.is(
                        savedPassWalk.getId().toString())));
    }

    @Test
    @SneakyThrows
    void getPasses_WithPartOfVisitorNameAndCarNumberWithFilterActive_ReturnListWithPassesWalkOnly() {
        saveTerritoryUserCarBrand();
        saveCar(); //LICENSE_PLATE = "А420ВХ799";
        PassAuto passAuto = createPassWithStatusAndTime(PassStatus.DELAYED, 5);
        Visitor visitor = new Visitor();
        visitor.setId(TestUtils.VISITOR_ID);
        visitor.setName("A420");
        visitor.setPhone(TestUtils.PHONE_NUM);
        PassWalk passWalk = PassTestData.getPassWalk(PassStatus.ACTIVE, LocalDateTime.now(),
                LocalDateTime.now().plusDays(1), savedUser,
                savedTerritory, visitor, PassTimeType.PERMANENT);
        passRepository.saveAndFlush(passAuto);
        PassWalk savedPassWalk = passRepository.saveAndFlush(passWalk);
        String filterParams = String.join(",", PassStatus.ACTIVE.name());

        ResultActions resultActions = mockMvc
                .perform(MockMvcUtils.getPasses()
                        .param(STATUS, filterParams)
                        .param("part", "A"));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id", Matchers.is(
                        savedPassWalk.getId().toString())));
    }

    @Test
    @SneakyThrows
    void getPasses_WithPartOfVisitorNameAndCarNumberWithEmptyParameterWithFilterActive_ReturnBothPasses() {
        saveTerritoryUserCarBrand();
        saveCar(); //LICENSE_PLATE = "А420ВХ799";
        PassAuto passAuto = createPassWithStatusAndTime(PassStatus.ACTIVE, 5);
        Visitor visitor = new Visitor();
        visitor.setId(TestUtils.VISITOR_ID);
        visitor.setName("A420");
        visitor.setPhone(TestUtils.PHONE_NUM);
        PassWalk passWalk = PassTestData.getPassWalk(PassStatus.ACTIVE, LocalDateTime.now(),
                LocalDateTime.now().plusDays(1), savedUser,
                savedTerritory, visitor, PassTimeType.PERMANENT);
        PassAuto savedPassAuto = passRepository.saveAndFlush(passAuto);
        PassWalk savedPassWalk = passRepository.saveAndFlush(passWalk);
        String filterParams = String.join(",", PassStatus.ACTIVE.name());

        ResultActions resultActions = mockMvc
                .perform(MockMvcUtils.getPasses()
                        .param(STATUS, filterParams)
                        .param("part", ""));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.size()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[*].id", Matchers.containsInAnyOrder(
                        savedPassWalk.getId().toString(), savedPassAuto.getId().toString())));
    }
}
