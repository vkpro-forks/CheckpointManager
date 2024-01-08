package ru.ac.checkpointmanager.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.RedisAndPostgresTestContainersConfiguration;
import ru.ac.checkpointmanager.dto.user.RegistrationDTO;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.util.TestMessage;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
public class AuthControllerIntegrationTest extends RedisAndPostgresTestContainersConfiguration {

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
    void register_ifEmailAlreadyExists_handleErrorAndReturn409() {
        User user = TestUtils.getUser();
        user.setEmail(TestUtils.EMAIL);
        userRepository.save(user);
        log.info("Saving user to repo");
        RegistrationDTO registrationDTO = TestUtils.getRegistrationDTO();
        String userAuthDtoString = TestUtils.jsonStringFromObject(registrationDTO);
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST, UrlConstants.AUTH_REG_URL);
        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.AUTH_REG_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userAuthDtoString))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL).value(Matchers.startsWith("[Email")))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_TITLE).value(Matchers.startsWith("Object")));
    }

}
