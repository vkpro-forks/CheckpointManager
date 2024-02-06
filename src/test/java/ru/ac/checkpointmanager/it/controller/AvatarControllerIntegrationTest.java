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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
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
import ru.ac.checkpointmanager.config.EnablePostgresAndRedisTestContainers;
import ru.ac.checkpointmanager.config.security.WithMockCustomUser;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.repository.AvatarRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.security.CustomAuthenticationToken;
import ru.ac.checkpointmanager.util.MockMvcUtils;
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
@EnablePostgresAndRedisTestContainers
class AvatarControllerIntegrationTest {

    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AvatarRepository avatarRepository;

    @Autowired
    TerritoryRepository territoryRepository;

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
        territoryRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void getAvatarImageByAvatarId_AllOk_ReturnAvatarImage() {
        //given
        Avatar avatar = TestUtils.getAvatar();
        Avatar savedAvatar = avatarRepository.saveAndFlush(avatar);
        User user = TestUtils.getUser();
        user.setRole(Role.USER);
        user.setAvatar(savedAvatar);
        User savedUser = userRepository.saveAndFlush(user);
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);
        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get(UrlConstants.AVATAR_AVATARS_URL, savedAvatar.getId())
                .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                .contentType(MediaType.APPLICATION_JSON));
        //then
        resultActions.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.IMAGE_JPEG_VALUE));
    }

    @Test
    @SneakyThrows
    @WithMockUser(roles = "ADMIN")
    void getAvatarImageByAvatarId_NoAvatar_HandleExceptionAndReturnNotFound() {
        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.get(UrlConstants.AVATAR_AVATARS_URL, TestUtils.AVATAR_ID));

        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.AVATAR_NOT_FOUND.formatted(TestUtils.AVATAR_ID)));
        ResultCheckUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    @WithMockUser(roles = "ADMIN")
    void getAvatarImageByTerritoryId_AllOk_ReturnAvatarImage() {
        Avatar avatar = TestUtils.getAvatar();
        Avatar savedAvatar = avatarRepository.saveAndFlush(avatar);
        Territory territory = TestUtils.getTerritory();
        territory.setAvatar(savedAvatar);
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get(UrlConstants.AVATAR_TERRITORY_URL, savedTerritory.getId())
                .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.IMAGE_JPEG_VALUE));
    }

    @Test
    @SneakyThrows
    @WithMockUser(roles = "ADMIN")
    void getAvatarImageByTerritoryId_NoTerritory_HandleExceptionAndReturnNotFound() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get(UrlConstants.AVATAR_TERRITORY_URL, TestUtils.TERR_ID)
                .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(TestUtils.TERR_ID)));
        ResultCheckUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    @WithMockUser(roles = "ADMIN")
    void getAvatarImageByTerritoryId_NoAvatar_HandleExceptionAndReturnNotFound() {
        Territory territory = TestUtils.getTerritory();
        Territory savedTerritory = territoryRepository.saveAndFlush(territory);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get(UrlConstants.AVATAR_TERRITORY_URL, savedTerritory.getId())
                .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.AVATAR_NOT_FOUND_FOR_TERRITORY.formatted(savedTerritory.getId())));
        ResultCheckUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void uploadAvatar_UserNotFound_HandleExceptionAndReturnNotFound() {
        MockMultipartFile file
                = new MockMultipartFile("avatarFile", "avatar.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3});

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.uploadAvatarForUser(TestUtils.USER_ID, file));

        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(TestUtils.USER_ID)));
        ResultCheckUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void deleteAvatarByUserId_AllOk_DeleteAvatarAndReturnNoContent() {
        Avatar avatar = TestUtils.getAvatar();
        Avatar savedAvatar = avatarRepository.saveAndFlush(avatar);
        User user = TestUtils.getUser();
        user.setRole(Role.USER);
        user.setAvatar(savedAvatar);
        User savedUser = userRepository.saveAndFlush(user);
        UUID userId = savedUser.getId();
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.delete(UrlConstants.AVATAR_URL + "/user/{userId}", userId)
                .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNoContent());
        Optional<Avatar> optionalAvatar = avatarRepository.findById(savedAvatar.getId());
        Assertions.assertThat(optionalAvatar).as("Check if avatar was deleted").isEmpty();
    }

    @Test
    @SneakyThrows
    void deleteAvatarByUserId_UserNotFound_HandleErrorAndReturnNotFound() {
        User notSavedUser = TestUtils.getUser();
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(notSavedUser);

        ResultActions resultActions = mockMvc
                .perform(MockMvcRequestBuilders.delete(UrlConstants.AVATAR_USER_URL, notSavedUser.getId())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(notSavedUser.getId())));
        ResultCheckUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void deleteAvatarByUserId_UserHasNoAvatar_HandleErrorAndReturnNotFound() {
        User user = TestUtils.getUser();
        user.setRole(Role.USER);
        User savedUser = userRepository.saveAndFlush(user);
        UUID userId = savedUser.getId();
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .delete(UrlConstants.AVATAR_USER_URL, userId)
                .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                .contentType(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(ExceptionUtils.AVATAR_NOT_FOUND_FOR_USER.formatted(savedUser.getId())));
        ResultCheckUtils.checkNotFoundFields(resultActions);
    }

}
