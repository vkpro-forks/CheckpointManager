package ru.ac.checkpointmanager.it;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.ac.checkpointmanager.config.EnablePostgresAndRedisTestContainers;
import ru.ac.checkpointmanager.extension.SavedPassWithRequiredEntitiesDTO;
import ru.ac.checkpointmanager.extension.annotation.PassWithRequiredEntities;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.util.MockMvcUtils;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = {"ADMIN"})
@EnablePostgresAndRedisTestContainers
class UserWithPassesIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    //https://junit.org/junit5/docs/current/user-guide/#extensions extension model in action
    @Test
    @SneakyThrows
    void deleteUser_UserWithPass_DeleteAndReturnNoContent(@PassWithRequiredEntities SavedPassWithRequiredEntitiesDTO passWithRequiredEntitiesDTO) {
        mockMvc.perform(MockMvcUtils.deleteUser(passWithRequiredEntitiesDTO.getUser().getId()))
                .andExpect(status().isNoContent());

        Assertions.assertThat(userRepository.existsByEmail(passWithRequiredEntitiesDTO.getUser().getEmail())).isFalse();
    }
}
