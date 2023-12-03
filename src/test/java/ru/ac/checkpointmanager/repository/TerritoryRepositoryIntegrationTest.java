package ru.ac.checkpointmanager.repository;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.instancio.Model;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.ac.checkpointmanager.it.config.CorsTestConfiguration;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.testcontainers.PostgresContainersConfig;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.List;

@Import({CorsTestConfiguration.class})
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Slf4j
class TerritoryRepositoryIntegrationTest extends PostgresContainersConfig {

    @Autowired
    UserRepository userRepository;

    @Autowired
    TerritoryRepository territoryRepository;

    @AfterEach
    void clear() {
        userRepository.deleteAll();
        territoryRepository.deleteAll();
    }

    @Test
    void shouldReturnUsersByTerritoryId() {
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        Model<User> userModel = TestUtils.getInstancioUserModel();
        List<User> users = Instancio.ofList(userModel).size(10).create();
        List<User> savedUsers = userRepository.saveAllAndFlush(users);
        territory.setUsers(savedUsers);
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);
        log.warn("!!!!!!!!!!!!BEFORE METHOD findUsersByTerritoryId!!!!!!!!!!!");
        List<User> usersByTerritoryId = territoryRepository.findUsersByTerritoryId(savedTerritory.getId());
        log.warn("!!!!!!!!!!!!AFTER METHOD findUsersByTerritoryId!!!!!!!!!!!");
        Assertions.assertThat(usersByTerritoryId).hasSize(10);
    }

}
