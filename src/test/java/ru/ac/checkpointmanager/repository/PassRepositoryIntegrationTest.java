package ru.ac.checkpointmanager.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.model.passes.PassTimeType;
import ru.ac.checkpointmanager.model.passes.PassWalk;
import ru.ac.checkpointmanager.util.TestUtils;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DirtiesContext
@Testcontainers
class PassRepositoryIntegrationTest {

    @Container
    @ServiceConnection(type = JdbcConnectionDetails.class)
    private static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("chpmanDB");

    @DynamicPropertySource
    private static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.liquibase.enabled", () -> true);
        registry.add("spring.liquibase.label-filter", () -> "!demo-data");
    }

    @Autowired
    PassRepository passRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TerritoryRepository territoryRepository;

    @Autowired
    VisitorRepository visitorRepository;

    @AfterEach
    void clear() {
        userRepository.deleteAll();
        territoryRepository.deleteAll();
        visitorRepository.deleteAll();
        passRepository.deleteAll();
    }

    @Test
    void findPassesByStatusAndTimeBefore_AllOk_ReturnListOfPasses() {
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        Visitor visitor = new Visitor();
        visitor.setName(TestUtils.FULL_NAME);
        visitor.setPhone(TestUtils.PHONE_NUM);
        Visitor savedVisitor = visitorRepository.saveAndFlush(visitor);

        PassStatus status = PassStatus.ACTIVE;
        PassWalk passWalk = new PassWalk();
        passWalk.setStatus(status);
        passWalk.setStartTime(LocalDateTime.now().minusDays(1));
        passWalk.setEndTime(LocalDateTime.now().plusDays(1));
        passWalk.setUser(savedUser);
        passWalk.setTerritory(savedTerritory);
        passWalk.setVisitor(savedVisitor);
        passWalk.setTimeType(PassTimeType.ONETIME);
        passRepository.saveAndFlush(passWalk);

        PassWalk passWalk2 = new PassWalk();
        passWalk2.setStatus(status);
        passWalk2.setStartTime(LocalDateTime.now().minusDays(1));
        passWalk2.setEndTime(LocalDateTime.now().plusDays(3));
        passWalk2.setUser(savedUser);
        passWalk2.setTerritory(territory);
        passWalk2.setVisitor(savedVisitor);
        passWalk2.setTimeType(PassTimeType.ONETIME);

        List<Pass> passes = passRepository.findPassesByStatusAndTimeBefore(status, "endTime", LocalDateTime.now().plusDays(2));

        Assertions.assertThat(passes).isNotEmpty();
        Assertions.assertThat(passes.get(0).getStatus()).isEqualTo(PassStatus.ACTIVE);
    }

    /*@Test
    void findPassesByStatusAndTimeBefore_ExperimentalEnumAndPgTypesDoesntMatch_ReturnListOfPasses() {
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        territory.setUsers(List.of(savedUser));
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        Visitor visitor = new Visitor();
        visitor.setName(TestUtils.FULL_NAME);
        visitor.setPhone(TestUtils.PHONE_NUM);
        Visitor savedVisitor = visitorRepository.saveAndFlush(visitor);

        PassStatus status = PassStatus.STATUS_NOT_IN_PG;
        PassWalk passWalk = new PassWalk();
        passWalk.setStatus(status);
        passWalk.setStartTime(LocalDateTime.now().minusDays(1));
        passWalk.setEndTime(LocalDateTime.now().plusDays(1));
        passWalk.setUser(savedUser);
        passWalk.setTerritory(savedTerritory);
        passWalk.setVisitor(savedVisitor);
        passWalk.setTimeType(PassTimeType.ONETIME);
        passRepository.saveAndFlush(passWalk);

        List<Pass> passes = passRepository.findPassesByStatusAndTimeBefore(status, "endTime", LocalDateTime.now().plusDays(2));

        Assertions.assertThat(passes).isNotEmpty();
        Assertions.assertThat(passes.get(0).getStatus()).isEqualTo(PassStatus.ACTIVE);
    }
*/

}
