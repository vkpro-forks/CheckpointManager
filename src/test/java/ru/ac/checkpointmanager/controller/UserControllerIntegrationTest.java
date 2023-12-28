package ru.ac.checkpointmanager.controller;

import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.CacheTestConfiguration;
import ru.ac.checkpointmanager.config.CorsTestConfiguration;
import ru.ac.checkpointmanager.config.PostgresTestContainersConfiguration;
import ru.ac.checkpointmanager.config.security.WithMockCustomUser;
import ru.ac.checkpointmanager.dto.user.ChangePasswordRequest;
import ru.ac.checkpointmanager.dto.user.UserPutDTO;
import ru.ac.checkpointmanager.model.Phone;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.PhoneRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.security.AuthFacade;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;
import ru.ac.checkpointmanager.utils.FieldsValidation;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@Import({CorsTestConfiguration.class, CacheTestConfiguration.class})
@ActiveProfiles("test")
class UserControllerIntegrationTest extends PostgresTestContainersConfiguration {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TerritoryRepository territoryRepository;

    @Autowired
    PhoneRepository phoneRepository;

    @Autowired
    private PasswordEncoder encoder;

    @MockBean
    AuthFacade authFacade;

    public static final String USER = "User";

    @AfterEach
    void clear() {
        userRepository.deleteAll();
        territoryRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void findUserByIdIsOK() {
        User user = TestUtils.getUserForDB();
        userRepository.saveAndFlush(user);
        UUID userId = user.getId();

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(userId.toString())))
                .andExpect(jsonPath("$.fullName", Matchers.is(user.getFullName())))
                .andExpect(jsonPath("$.email", Matchers.is(user.getEmail())));
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void findUserByIdIsNotFound() {
        UUID userId = UUID.randomUUID();

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/{id}", userId))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(USER)));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    @WithAnonymousUser
    void findUserByIdIsUnauthorized() {
        UUID userId = TestUtils.USER_ID;

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/{id}", userId))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());

    }

    @Test
    @SneakyThrows
    @WithMockCustomUser(role = "USER")
    void getTerritoriesByUserIsOKWithRightId() {
        User user = TestUtils.getUserForDB();
        Territory territory = TestUtils.getTerritoryForDB();
        territoryRepository.saveAndFlush(territory);
        List<Territory> territories = List.of(territory);
        user.setTerritories(territories);
        userRepository.saveAndFlush(user);
        UUID userId = user.getId();

        Mockito.when(authFacade.isUserIdMatch(userId)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/{userId}/territories", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", Matchers.is(territories.size())))
                .andExpect(jsonPath("$[*].name", Matchers.hasItem(territory.getName())))
                .andExpect(jsonPath("$[*].id", Matchers.hasItem(territory.getId().toString())));
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser(role = "USER")
    void getTerritoriesByUserIsForbiddenWithWrongId() {
        UUID userId = TestUtils.USER_ID;
        Mockito.when(authFacade.isUserIdMatch(userId)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/{userId}/territories", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void getTerritoriesByUserIsOkWithRoleAdminAndAnotherId() {
        User user = TestUtils.getUserForDB();
        Territory territory = TestUtils.getTerritoryForDB();
        territoryRepository.saveAndFlush(territory);
        List<Territory> territories = List.of(territory);
        user.setTerritories(territories);
        userRepository.saveAndFlush(user);
        UUID userId = user.getId();

        Mockito.when(authFacade.isUserIdMatch(userId)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/{userId}/territories", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", Matchers.is(territories.size())))
                .andExpect(jsonPath("$[*].name", Matchers.hasItem(territory.getName())))
                .andExpect(jsonPath("$[*].id", Matchers.hasItem(territory.getId().toString())));
    }

    @Test
    @SneakyThrows
    @WithAnonymousUser
    void getTerritoriesByUserIsUnauthorized() {
        UUID userId = TestUtils.USER_ID;

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/{userId}/territories", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void findUserByNameIsOk() {
        User user = TestUtils.getUserForDB();
        userRepository.saveAndFlush(user);
        String name = user.getFullName();

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/name")
                        .param("name", name)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[*].fullName", Matchers.hasItem(user.getFullName())))
                .andExpect(jsonPath("$[*].id", Matchers.hasItem(user.getId().toString())));
    }

    @Test
    @SneakyThrows
    @WithAnonymousUser
    void findUserByNameIsUnauthorized() {
        String name = TestUtils.USER_NAME;
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/name")
                        .param("name", name)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void getAllIsOkWithAdminRole() {
        User user = TestUtils.getUserForDB();
        userRepository.saveAndFlush(user);

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", Matchers.hasItem(user.getId().toString())))
                .andExpect(jsonPath("$[*].fullName", Matchers.hasItem(user.getFullName())))
                .andExpect(jsonPath("$[*].email", Matchers.hasItem(user.getEmail())));
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser(role = "USER")
    void getAllIsForbiddenWithUserRole() {
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithAnonymousUser
    void getAllIsUnauthorized() {
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser(role = "USER")
    void findUsersPhoneNumbersIsOkWithRightId() {
        User user = TestUtils.getUserForDB();
        Phone phone = TestUtils.getPhoneForDB();
        userRepository.saveAndFlush(user);
        phone.setUser(user);
        phoneRepository.saveAndFlush(phone);
        user.getNumbers().add(phone);
        userRepository.saveAndFlush(user);
        UUID userId = user.getId();

        Mockito.when(authFacade.isUserIdMatch(userId)).thenReturn(true);
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/numbers/{id}", userId).
                        contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*]", Matchers.hasItem(phone.getNumber())));
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser(role = "USER")
    void findUsersPhoneNumbersIsForbiddenWithWrongId() {
        User user = TestUtils.getUserForDB();
        Phone phone = TestUtils.getPhoneForDB();
        userRepository.saveAndFlush(user);
        phone.setUser(user);
        phoneRepository.saveAndFlush(phone);
        user.getNumbers().add(phone);
        userRepository.saveAndFlush(user);
        UUID userId = user.getId();


        Mockito.when(authFacade.isUserIdMatch(userId)).thenReturn(false);
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/numbers/{id}", userId).
                        contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void findUsersPhoneNumbersIsOkWithRoleAdminAndAnyId() {
        User user = TestUtils.getUserForDB();
        Phone phone = TestUtils.getPhoneForDB();
        userRepository.saveAndFlush(user);
        phone.setUser(user);
        phoneRepository.saveAndFlush(phone);
        user.getNumbers().add(phone);
        userRepository.saveAndFlush(user);
        UUID userId = user.getId();

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/numbers/{id}", userId).
                        contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*]", Matchers.hasItem(phone.getNumber())));
    }

    @Test
    @SneakyThrows
    @WithAnonymousUser
    void findUsersPhoneNumbersIsUnauthorized() {
        User user = TestUtils.getUserForDB();
        userRepository.saveAndFlush(user);
        UUID userId = user.getId();

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/numbers/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser(role = "USER")
    void updateUserIsOkWithRightUserId() {
        User user = TestUtils.getUserForDB();
        userRepository.saveAndFlush(user);
        UUID userId = user.getId();
        UserPutDTO userPutDTO = TestUtils.getUserPutDTO();
        userPutDTO.setId(userId);
        userPutDTO.setMainNumber(FieldsValidation.cleanPhone(userPutDTO.getMainNumber()));
        String userPutDTOString = TestUtils.jsonStringFromObject(userPutDTO);

        Mockito.when(authFacade.isUserIdMatch(userId)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.USER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userPutDTOString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(userPutDTO.getId().toString())))
                .andExpect(jsonPath("$.fullName", Matchers.is(userPutDTO.getFullName())))
                .andExpect(jsonPath("$.mainNumber", Matchers.is(userPutDTO.getMainNumber())));
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void updateUserIsOkWithRoleAdmin() {
        User user = TestUtils.getUserForDB();
        userRepository.saveAndFlush(user);
        UUID userId = user.getId();
        UserPutDTO userPutDTO = TestUtils.getUserPutDTO();
        userPutDTO.setId(userId);
        userPutDTO.setMainNumber(FieldsValidation.cleanPhone(userPutDTO.getMainNumber()));
        String userPutDTOString = TestUtils.jsonStringFromObject(userPutDTO);

        mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.USER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userPutDTOString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(userPutDTO.getId().toString())))
                .andExpect(jsonPath("$.fullName", Matchers.is(userPutDTO.getFullName())))
                .andExpect(jsonPath("$.mainNumber", Matchers.is(userPutDTO.getMainNumber())));
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser(role = "USER")
    void updateUserIsForbiddenWithWrongUserId() {
        User user = TestUtils.getUserForDB();
        userRepository.saveAndFlush(user);
        UUID userId = user.getId();
        UserPutDTO userPutDTO = TestUtils.getUserPutDTO();
        String userPutDTOString = TestUtils.jsonStringFromObject(userPutDTO);

        Mockito.when(authFacade.isUserIdMatch(userId)).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.USER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userPutDTOString))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser(role = "SECURITY")
    void updateUserIsForbiddenWithRoleSecurity() {
        UserPutDTO userPutDTO = TestUtils.getUserPutDTO();
        String userPutDTOString = TestUtils.jsonStringFromObject(userPutDTO);

        mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.USER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userPutDTOString))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void updateUserIsBadRequest() {
        User user = TestUtils.getUserForDB();
        userRepository.saveAndFlush(user);
        UUID userId = user.getId();
        UserPutDTO userPutDTO = TestUtils.getUserPutDTO();
        userPutDTO.setId(userId);
        userPutDTO.setMainNumber("integration tests sucks");
        userPutDTO.setFullName("have u seen capital letter?");
        String userPutDTOString = TestUtils.jsonStringFromObject(userPutDTO);

        mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.USER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userPutDTOString))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.title").isNotEmpty());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void updateUserIsNotFound() {
        UserPutDTO userPutDTO = TestUtils.getUserPutDTO();
        String userPutDTOString = TestUtils.jsonStringFromObject(userPutDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.USER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPutDTOString));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser(username = "123@123.com", role = "USER")
    void changePasswordIsNoContent() {
        User user = TestUtils.getUserForDB();
        user.setPassword(encoder.encode(user.getPassword()));
        userRepository.saveAndFlush(user);

        ChangePasswordRequest request = TestUtils.getChangePasswordRequest();
        String requestString = TestUtils.jsonStringFromObject(request);

        Mockito.when(authFacade.getCurrentUser()).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isNoContent());
    }
}
