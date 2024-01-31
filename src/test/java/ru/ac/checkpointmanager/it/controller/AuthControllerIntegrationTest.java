package ru.ac.checkpointmanager.it.controller;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.EnablePostgresAndRedisTestContainers;
import ru.ac.checkpointmanager.dto.user.AuthRequestDTO;
import ru.ac.checkpointmanager.dto.user.RefreshTokenDTO;
import ru.ac.checkpointmanager.dto.user.RegistrationDTO;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.util.TestMessage;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
@EnablePostgresAndRedisTestContainers
class AuthControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    UserRepository userRepository;

    @AfterEach
    void clear() {
        userRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void registerIsCreated() {
        RegistrationDTO registrationDTO = TestUtils.getRegistrationDTO();
        String registrationDTOString = TestUtils.jsonStringFromObject(registrationDTO);

        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.AUTH_REG_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationDTOString))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.fullName", Matchers.is(registrationDTO.getFullName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", Matchers.is(registrationDTO.getEmail())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.verifiedToken", Matchers.notNullValue()));
    }

    @Test
    @SneakyThrows
    void registerIsBadRequest() {
        RegistrationDTO registrationDTO = TestUtils.getRegistrationDTO();
        registrationDTO.setFullName("у попа была собака");
        registrationDTO.setEmail("плохой емайл");
        registrationDTO.setPassword("пароль с пробелами");
        String registrationDTOString = TestUtils.jsonStringFromObject(registrationDTO);

        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.AUTH_REG_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationDTOString))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Validation error"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Validation failed"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.violations[*].name")
                        .value(Matchers.containsInAnyOrder("email", "fullName", "password")));
    }

    @Test
    @SneakyThrows
    void register_ifEmailAlreadyExists_handleErrorAndReturn409() {
        User user = TestUtils.getUser();
        user.setEmail(TestUtils.EMAIL);
        userRepository.saveAndFlush(user);
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

    @Test
    @SneakyThrows
    void authenticateIsOk() {
        User user = TestUtils.getUser();
        String email = user.getEmail();
        String password = user.getPassword();
        user.setIsBlocked(false);
        user.setPassword(encoder.encode(password));
        userRepository.saveAndFlush(user);
        AuthRequestDTO request = new AuthRequestDTO(email, password);
        String requestString = TestUtils.jsonStringFromObject(request);
        log.info("Performing authentication request for Email: {}", email);
        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.AUTH_LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andDo(result -> log.info("Response: {}", result.getResponse().getContentAsString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email", Matchers.is(email)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.access_token", Matchers.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.refresh_token", Matchers.notNullValue()));
    }

    @Test
    @SneakyThrows
    void authenticateIsUnauthorizedWhenUserLocked() {
        User user = TestUtils.getUser();
        String email = user.getEmail();
        String password = user.getPassword();
        user.setIsBlocked(true);
        user.setPassword(encoder.encode(password));
        userRepository.saveAndFlush(user);
        AuthRequestDTO request = new AuthRequestDTO(email, password);
        String requestString = TestUtils.jsonStringFromObject(request);
        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.AUTH_LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Authentication error"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("User account is locked"));
    }

    @Test
    @SneakyThrows
    void authenticateIsUnauthorizedWhenBadCredentials() {
        User user = TestUtils.getUser();
        user.setIsBlocked(false);
        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.saveAndFlush(user);
        AuthRequestDTO request = new AuthRequestDTO("email", "password");
        String requestString = TestUtils.jsonStringFromObject(request);

        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.AUTH_LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Authentication error"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail").value("Bad credentials"));
    }

    @Test
    @SneakyThrows
    void refreshTokenIsOk() {
        User user = TestUtils.getUserForDB();
        user.setRole(Role.ADMIN);
        userRepository.saveAndFlush(user);
        RefreshTokenDTO refreshTokenDTO = TestUtils.getRefreshTokenDTO();
        String refreshTokenString = TestUtils.jsonStringFromObject(refreshTokenDTO);

        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.AUTH_REFRESH_TOKEN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshTokenString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.access_token", Matchers.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.refresh_token", Matchers.notNullValue()));
    }

    @Test
    @SneakyThrows
    void refreshTokenIsUnauthorized() {
        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO("невалидный_токен");
        String refreshTokenString = TestUtils.jsonStringFromObject(refreshTokenDTO);

        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.AUTH_REFRESH_TOKEN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshTokenString))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Jwt is invalid"));
    }

    @Test
    @SneakyThrows
    void isUserAuthenticatedWhenUserExistOk() {
        User user = TestUtils.getUser();
        userRepository.saveAndFlush(user);
        String email = user.getEmail();

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.AUTH_URL + "/is-authenticated")
                        .param("email", email))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.fullName", Matchers.is(user.getFullName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.isAuthenticated", Matchers.is(true)));
    }

    @Test
    @SneakyThrows
    void isUserAuthenticatedWhenUserNotExistOk() {
        String email = TestUtils.EMAIL;

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.AUTH_URL + "/is-authenticated")
                        .param("email", email))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.fullName", Matchers.nullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.isAuthenticated", Matchers.is(false)));
    }
}
