package ru.ac.checkpointmanager.it;

import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.security.WithMockCustomUser;
import ru.ac.checkpointmanager.dto.user.ConfirmChangeEmail;
import ru.ac.checkpointmanager.dto.user.UserPutDTO;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.UUID;

class UserNotFoundExceptionHandlerTest extends GlobalExceptionHandlerBasicTestConfig {

    public static final String USER = "User";

    @Autowired
    UserRepository userRepository;

    @Autowired
    TerritoryRepository territoryRepository;

    @Autowired
    CacheManager cacheManager;


    @AfterEach
    void clear() {
        userRepository.deleteAll();
        territoryRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void shouldHandleUserNotFoundExceptionForAddPass() {
        String passDtoCreate = TestUtils.jsonStringFromObject(TestUtils.getPassCreateDTOWithCar());
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passDtoCreate))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(USER)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleUserNotFoundExceptionForGetPassesByUser() {
        String passDtoCreate = TestUtils.jsonStringFromObject(TestUtils.getPassCreateDTOWithCar());
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(passDtoCreate))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(USER)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void shouldHandleUserNotFoundExceptionForUploadAvatar() {
        MockMultipartFile file
                = new MockMultipartFile("avatarFile", "avatar.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3});
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.multipart(
                        HttpMethod.POST, UrlConstants.AVATAR_URL + "/" + TestUtils.USER_ID).file(file))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(USER)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleUserNotFoundExceptionForAttachUserToTerritory() {
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        Territory savedTerritory = territoryRepository.save(territory);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .patch(UrlConstants.TERR_ATTACH_DETACH_URL
                                .formatted(savedTerritory.getId(), TestUtils.USER_ID)))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(USER)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleUserNotFoundExceptionForDetachUserToTerritory() {
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        Territory savedTerritory = territoryRepository.save(territory);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .delete(UrlConstants.TERR_ATTACH_DETACH_URL
                                .formatted(savedTerritory.getId(), TestUtils.USER_ID)))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(USER)));
        TestUtils.checkNotFoundFields(resultActions);
    }


    @Test
    @SneakyThrows
    void shouldHandleUserNotFoundExceptionForGetUser() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get(UrlConstants.USER_URL + "/" + TestUtils.USER_ID))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(USER)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser(id = "bf03cd3b-8b20-4cac-8be9-e1cdf825c165")
    void shouldHandleUserNotFoundExceptionForDeleteUser() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .delete(UrlConstants.USER_URL + "/" + "bf03cd3b-8b20-4cac-8be9-e1cdf825c165"))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(USER)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleUserNotFoundExceptionForConfirmEmail() {
        Cache emailCache = cacheManager.getCache("email");
        ConfirmChangeEmail changeEmail = TestUtils.getConfirmChangeEmail();
        assert emailCache != null;
        emailCache.put(changeEmail.getVerifiedToken(), changeEmail);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get(UrlConstants.CONFIRM_EMAIL_URL)
                        .param("token", changeEmail.getVerifiedToken()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(USER)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleUserNotFoundExceptionForChangeRole() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .patch(UrlConstants.USER_ROLE_URL + "/" + TestUtils.USER_ID)
                        .param("role", "ADMIN"))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(USER)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser(id = "bf03cd3b-8b20-4cac-8be9-e1cdf825c165")
    void shouldHandleUserNotFoundExceptionForUpdateUser() {
        UserPutDTO userPutDto = TestUtils.getUserPutDTO();
        userPutDto.setId(UUID.fromString("bf03cd3b-8b20-4cac-8be9-e1cdf825c165"));
        String userPutDtoStr = TestUtils.jsonStringFromObject(userPutDto);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .put(UrlConstants.USER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userPutDtoStr))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(USER)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleUserNotFoundExceptionForUpdateBlockStatus() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .patch(UrlConstants.USER_URL + "/" + TestUtils.USER_ID)
                        .param("isBlocked", "true"))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(USER)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldHandleUserNotFoundExceptionForGettingRefreshToken() {
        String refreshTokenString = TestUtils.jsonStringFromObject(TestUtils.getRefreshTokenDTO());
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .post(UrlConstants.AUTH_REFRESH_TOKEN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshTokenString))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(USER)));
        TestUtils.checkNotFoundFields(resultActions);
    }

}
