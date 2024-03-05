package ru.ac.checkpointmanager.it;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.ac.checkpointmanager.config.EnablePostgresAndRedisTestContainers;
import ru.ac.checkpointmanager.ext.UserTerritoryCarPassInRepositoryExtension;
import ru.ac.checkpointmanager.model.car.Car;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = {"ADMIN"})
@EnablePostgresAndRedisTestContainers
@ExtendWith(UserTerritoryCarPassInRepositoryExtension.class)
public class UserWithPassesIntegrationTest {

    private Car car;

    @Test
    void test() {
        System.out.println("asdfasfasdf" + car);
        Assertions.assertThat(car).isNotNull();
        System.out.println("hahah");
    }
}
