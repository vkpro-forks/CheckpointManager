package ru.ac.checkpointmanager.it.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.ac.checkpointmanager.config.RedisAndPostgresTestContainersConfiguration;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.repository.AvatarRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.security.CustomAuthenticationToken;
import ru.ac.checkpointmanager.util.ResultCheckUtils;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles("test")
@Slf4j
class AvatarControllerIntegrationTest extends RedisAndPostgresTestContainersConfiguration {

    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AvatarRepository avatarRepository;

    @Autowired
    WebApplicationContext context;

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @AfterEach
    void clear() {
        avatarRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void deleteAvatarByUserId_AllOk_DeleteAvatarAndReturnNoContent() {
        //given
        Avatar avatar = TestUtils.getAvatar();
        Avatar savedAvatar = avatarRepository.saveAndFlush(avatar);
        User user = TestUtils.getUser();
        user.setRole(Role.USER);
        user.setAvatar(savedAvatar);
        User savedUser = userRepository.saveAndFlush(user);
        UUID userId = savedUser.getId();
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);
        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.delete(UrlConstants.AVATAR_URL + "/user/{userId}", userId)
                .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                .contentType(MediaType.APPLICATION_JSON));
        //then
        resultActions.andExpect(status().isNoContent());
        Optional<Avatar> optionalAvatar = avatarRepository.findById(savedAvatar.getId());
        Assertions.assertThat(optionalAvatar).as("Check if avatar was deleted").isEmpty();
    }

    @Test
    @SneakyThrows
    void deleteAvatarByUserId_UserNotFound_HandleErrorAndReturnNotFound() {
        //given
        User notSavedUser = TestUtils.getUser();
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(notSavedUser);
        //when
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.delete(UrlConstants.AVATAR_URL + "/user/{userId}", notSavedUser.getId())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON));
        //then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(notSavedUser.getId())));
        ResultCheckUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void deleteAvatarByUserId_UserHasNoAvatar_HandleErrorAndReturnNotFound() {
        //given
        User user = TestUtils.getUser();
        user.setRole(Role.USER);
        User savedUser = userRepository.saveAndFlush(user);
        UUID userId = savedUser.getId();
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);
        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .delete(UrlConstants.AVATAR_URL + "/user/{userId}", userId)
                .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                .contentType(MediaType.APPLICATION_JSON));
        //then
        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.AVATAR_NOT_FOUND_FOR_USER.formatted(savedUser.getId())));
        ResultCheckUtils.checkNotFoundFields(resultActions);
    }

}
