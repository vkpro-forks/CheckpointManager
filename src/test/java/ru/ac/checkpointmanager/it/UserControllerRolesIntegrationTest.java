package ru.ac.checkpointmanager.it;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.CorsTestConfiguration;
import ru.ac.checkpointmanager.config.PostgresTestContainersConfiguration;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import({CorsTestConfiguration.class})
@ActiveProfiles("test")
class UserControllerRolesIntegrationTest extends PostgresTestContainersConfiguration {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @AfterEach
    void clear() {
        userRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void shouldPassWithGoodTokenAndGetResult() {
        User user = TestUtils.getUser();
        user.setRole(Role.ADMIN);
        User savedUser = userRepository.saveAndFlush(user);

        String jwt = TestUtils.getJwt(1000000, savedUser.getUsername(), List.of("ROLE_ADMIN"), false, true);
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL)
                        .header(TestUtils.AUTH_HEADER, TestUtils.BEARER + jwt))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}
