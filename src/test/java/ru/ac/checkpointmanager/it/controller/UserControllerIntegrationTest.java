package ru.ac.checkpointmanager.it.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
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
import ru.ac.checkpointmanager.config.security.WithMockCustomUser;
import ru.ac.checkpointmanager.dto.user.NewEmailDTO;
import ru.ac.checkpointmanager.dto.user.NewPasswordDTO;
import ru.ac.checkpointmanager.dto.user.UserUpdateDTO;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.handler.ErrorCode;
import ru.ac.checkpointmanager.model.Phone;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.enums.PhoneNumberType;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.repository.PhoneRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.security.CustomAuthenticationToken;
import ru.ac.checkpointmanager.util.TestMessage;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;
import ru.ac.checkpointmanager.utils.FieldsValidation;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles("test")
@Slf4j
class UserControllerIntegrationTest extends RedisAndPostgresTestContainersConfiguration {

    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TerritoryRepository territoryRepository;

    @Autowired
    PhoneRepository phoneRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    WebApplicationContext context;

    User savedUser;

    public static final String USER = "User";

    @BeforeEach
    void init() {
        User user = TestUtils.getUser();
        user.setRole(Role.USER);
        savedUser = userRepository.saveAndFlush(user);
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @AfterEach
    void clear() {
        userRepository.deleteAll();
        territoryRepository.deleteAll();
        phoneRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void findUserByIdIsOK() {
        UUID userId = savedUser.getId();
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/{id}", userId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(userId.toString())))
                .andExpect(jsonPath("$.fullName", Matchers.is(savedUser.getFullName())))
                .andExpect(jsonPath("$.email", Matchers.is(savedUser.getEmail())));
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
    void getTerritoriesByUserIsOKWithRightId() {
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);
        log.info("Authorization token: {}", authToken);
        Territory territory = TestUtils.getTerritoryForDB();
        territory.setUsers(List.of(savedUser));
        territoryRepository.saveAndFlush(territory);
        List<Territory> territories = List.of(territory);
        UUID userId = savedUser.getId();

        log.info(TestMessage.PERFORM_HTTP, HttpMethod.GET.name(),
                UrlConstants.USER_TERR_URL.formatted(savedUser.getId()));
        log.info("saved user id {}", userId);
        log.info("Auth {}", authToken.getUserId());
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/{userId}/territories", userId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()", Matchers.is(territories.size())))
                .andExpect(jsonPath("$[*].name", Matchers.hasItem(territory.getName())))
                .andExpect(jsonPath("$[*].id", Matchers.hasItem(territory.getId().toString())));
    }

    @Test
    @SneakyThrows
    void getTerritoriesByUserIsForbiddenWithWrongId() {
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);
        UUID anotherID = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/{userId}/territories", anotherID)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
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
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", Matchers.hasItem(savedUser.getId().toString())))
                .andExpect(jsonPath("$[*].fullName", Matchers.hasItem(savedUser.getFullName())))
                .andExpect(jsonPath("$[*].email", Matchers.hasItem(savedUser.getEmail())));
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
    void findUsersPhoneNumbersIsOkWithRightId() {
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);
        Phone phone = TestUtils.getPhoneForDB();
        phone.setUser(savedUser);
        phoneRepository.saveAndFlush(phone);
        savedUser.getNumbers().add(phone);
        userRepository.saveAndFlush(savedUser);
        UUID userId = savedUser.getId();

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/numbers/{id}", userId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[*]", Matchers.hasItem(phone.getNumber())));
    }

    @Test
    @SneakyThrows
    void findUsersPhoneNumbersIsForbiddenWithWrongId() {
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);
        UUID anotherId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/numbers/{id}", anotherId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void findUsersPhoneNumbersIsOkWithRoleAdminAndAnyId() {
        Phone phone = TestUtils.getPhoneForDB();
        phone.setUser(savedUser);
        phoneRepository.saveAndFlush(phone);
        savedUser.getNumbers().add(phone);
        userRepository.saveAndFlush(savedUser);
        UUID userId = savedUser.getId();

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
        UUID userId = TestUtils.USER_ID;

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL + "/numbers/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @SneakyThrows
    void updateUserIsOkWithRightUserId() {
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);
        UUID userId = savedUser.getId();
        UserUpdateDTO userUpdateDTO = TestUtils.getUserUpdateDTO();
        userUpdateDTO.setId(userId);
        userUpdateDTO.setMainNumber(FieldsValidation.cleanPhone(userUpdateDTO.getMainNumber()));
        String userPutDTOString = TestUtils.jsonStringFromObject(userUpdateDTO);

        mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.USER_URL)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userPutDTOString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(userUpdateDTO.getId().toString())))
                .andExpect(jsonPath("$.fullName", Matchers.is(userUpdateDTO.getFullName())))
                .andExpect(jsonPath("$.mainNumber", Matchers.is(userUpdateDTO.getMainNumber())));
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void updateUserIsOkWithRoleAdmin() {
        UUID userId = savedUser.getId();
        UserUpdateDTO userUpdateDTO = TestUtils.getUserUpdateDTO();
        userUpdateDTO.setId(userId);
        userUpdateDTO.setMainNumber(FieldsValidation.cleanPhone(userUpdateDTO.getMainNumber()));
        String userPutDTOString = TestUtils.jsonStringFromObject(userUpdateDTO);

        mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.USER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userPutDTOString))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(userUpdateDTO.getId().toString())))
                .andExpect(jsonPath("$.fullName", Matchers.is(userUpdateDTO.getFullName())))
                .andExpect(jsonPath("$.mainNumber", Matchers.is(userUpdateDTO.getMainNumber())));
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void updateUser_IfPhoneNumberExists_HandleAndReturnConflictError() {
        //given
        UUID userId = savedUser.getId();
        UserUpdateDTO userUpdateDTO = TestUtils.getUserUpdateDTO();
        userUpdateDTO.setId(userId);
        String cleanedPhone = FieldsValidation.cleanPhone(userUpdateDTO.getMainNumber());
        userUpdateDTO.setMainNumber(cleanedPhone);
        Phone phone = new Phone();
        phone.setNumber(cleanedPhone);
        phone.setType(PhoneNumberType.MOBILE);
        phone.setUser(savedUser);
        phoneRepository.saveAndFlush(phone);
        String userPutDTOString = TestUtils.jsonStringFromObject(userUpdateDTO);
        //when
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.USER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPutDTOString));
        //then
        resultActions.andExpect(status().isConflict())
                .andExpect(jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.CONFLICT.toString()))
                .andExpect(jsonPath(TestUtils.JSON_DETAIL).value(ExceptionUtils.PHONE_EXISTS.formatted(cleanedPhone)));
    }

    @Test
    @SneakyThrows
    void updateUserIsForbiddenWithWrongUserId() {
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);
        UUID userId = UUID.randomUUID();
        UserUpdateDTO userUpdateDTO = TestUtils.getUserUpdateDTO();
        userUpdateDTO.setId(userId);
        String userPutDTOString = TestUtils.jsonStringFromObject(userUpdateDTO);

        mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.USER_URL)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userPutDTOString))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void updateUserIsBadRequest() {
        UUID userId = savedUser.getId();
        UserUpdateDTO userUpdateDTO = TestUtils.getUserUpdateDTO();
        userUpdateDTO.setId(userId);
        userUpdateDTO.setMainNumber("integration tests sucks");
        userUpdateDTO.setFullName("have u seen capital letter?");
        String userPutDTOString = TestUtils.jsonStringFromObject(userUpdateDTO);

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
        UserUpdateDTO userUpdateDTO = TestUtils.getUserUpdateDTO();
        String userPutDTOString = TestUtils.jsonStringFromObject(userUpdateDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.USER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPutDTOString));
        TestUtils.checkNotFoundFields(resultActions);
    }

    @Test
    @SneakyThrows
    void changePasswordIsNoContent() {
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);
        NewPasswordDTO request = TestUtils.getNewPasswordDTO();
        request.setCurrentPassword(savedUser.getPassword());
        savedUser.setPassword(encoder.encode(savedUser.getPassword()));
        userRepository.saveAndFlush(savedUser);
        String requestString = TestUtils.jsonStringFromObject(request);

        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/password")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    void changePasswordIdConflictWithWrongCurrentPassword() {
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);
        NewPasswordDTO request = TestUtils.getNewPasswordDTO();
        savedUser.setPassword(encoder.encode(savedUser.getPassword()));
        userRepository.saveAndFlush(savedUser);
        String requestString = TestUtils.jsonStringFromObject(request);

        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/password")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").isNotEmpty());
    }

    @Test
    @SneakyThrows
    void changePasswordIsBadRequest() {
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);
        NewPasswordDTO request = TestUtils.getNewPasswordDTO();
        request.setCurrentPassword(savedUser.getPassword());
        request.setConfirmationPassword("some wrong password");
        savedUser.setPassword(encoder.encode(savedUser.getPassword()));
        userRepository.saveAndFlush(savedUser);
        String requestString = TestUtils.jsonStringFromObject(request);

        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/password")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestString))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").isNotEmpty());
    }

    @Test
    @SneakyThrows
    void changeEmailIsOk() {
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);
        NewEmailDTO request = TestUtils.getNewEmailDTO();
        String requestString = TestUtils.jsonStringFromObject(request);

        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/email")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .content(requestString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void changeEmailIsBadRequestWhenEmailAlreadyExist() {
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);
        NewEmailDTO request = TestUtils.getNewEmailDTO();
        request.setNewEmail(savedUser.getEmail());
        String requestString = TestUtils.jsonStringFromObject(request);

        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/email")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
                        .content(requestString)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").isNotEmpty());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void changeRoleIsNoContentWithAdminRole() {
        UUID userId = savedUser.getId();
        String newRole = Role.MANAGER.name();

        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/role/{id}", userId)
                        .param("role", newRole))
                .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser(role = "MANAGER")
    void changeRoleIsForbiddenWhenManagerChangeRoleToAdmin() {
        UUID userId = savedUser.getId();
        String newRole = Role.ADMIN.name();

        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/role/{id}", userId)
                        .param("role", newRole))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser(role = "MANAGER")
    void changeRoleIsForbiddenWhenManagerChangeRoleFromAdminToAnother() {
        savedUser.setRole(Role.ADMIN);
        userRepository.saveAndFlush(savedUser);
        UUID userId = savedUser.getId();
        String newRole = Role.USER.name();

        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/role/{id}", userId)
                        .param("role", newRole))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void changeRoleIsConflictWhenNewRoleMatchedPrevious() {
        UUID userId = savedUser.getId();
        String newRole = Role.USER.name();

        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/role/{id}", userId)
                        .param("role", newRole))
                .andExpect(status().isConflict());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void changeRoleIsUserNotFound() {
        UUID userId = UUID.randomUUID();
        String newRole = Role.SECURITY.name();

        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/role/{id}", userId)
                        .param("role", newRole))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void updateBlockStatusIsOk() {
        UUID userId = savedUser.getId();
        Boolean isBlocked = true;

        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/{id}", userId)
                        .param("isBlocked", isBlocked.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser(role = "USER")
    void updateBlockStatusIsForbidden() {
        UUID userId = savedUser.getId();
        Boolean isBlocked = true;

        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/{id}", userId)
                        .param("isBlocked", isBlocked.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void updateBlockStatusIsNotFound() {
        UUID userId = UUID.randomUUID();
        Boolean isBlocked = true;

        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/{id}", userId)
                        .param("isBlocked", isBlocked.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void blockByIdIsNoContent() {
        UUID userId = savedUser.getId();

        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/block/{id}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser(role = "USER")
    void blockByIdIsForbidden() {
        UUID userId = savedUser.getId();

        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/block/{id}", userId))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void blockByIdIsNotFound() {
        UUID userId = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/block/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void unblockByIdIsNoContent() {
        UUID userId = savedUser.getId();

        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/unblock/{id}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser(role = "USER")
    void unblockByIdIsForbidden() {
        UUID userId = savedUser.getId();

        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/unblock/{id}", userId))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void unblockByIdIsNotFound() {
        UUID userId = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.patch(UrlConstants.USER_URL + "/unblock/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void deleteUserIsNoContentWithRightId() {
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);
        UUID userId = savedUser.getId();

        mockMvc.perform(MockMvcRequestBuilders.delete(UrlConstants.USER_URL + "/{id}", userId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken)))
                .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    void deleteUserIsForbiddenWithWrongId() {
        CustomAuthenticationToken authToken = TestUtils.getAuthToken(savedUser);
        UUID userId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.delete(UrlConstants.USER_URL + "/{id}", userId)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void deleteUserIsNoContentWithAdminRole() {
        UUID userId = savedUser.getId();

        mockMvc.perform(MockMvcRequestBuilders.delete(UrlConstants.USER_URL + "/{id}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void deleteUserIsNotFound() {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.delete(UrlConstants.USER_URL + "/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @SneakyThrows
    void shouldReturnEmptyListWithTerritoriesFromDBAndFromCache() {
        Collection<? extends GrantedAuthority> authorities = List
                .of(new SimpleGrantedAuthority(savedUser.getRole().name()));
        CustomAuthenticationToken authToken = new CustomAuthenticationToken(savedUser, null, savedUser.getId(),
                authorities);
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.GET.name(),
                UrlConstants.USER_TERR_URL.formatted(savedUser.getId()));
        log.info("Empty list from database");
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_TERR_URL
                                .formatted(savedUser.getId()))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
        log.info("Empty list from Cache");
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_TERR_URL
                                .formatted(savedUser.getId()))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty());
    }

    @Test
    @SneakyThrows
    void shouldReturnListWithTerritoriesFromDBAndFromCache() {
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        territory.setUsers(List.of(savedUser));
        territoryRepository.saveAndFlush(territory);
        Collection<? extends GrantedAuthority> authorities = List
                .of(new SimpleGrantedAuthority(savedUser.getRole().name()));
        CustomAuthenticationToken authToken = new CustomAuthenticationToken(savedUser, null, savedUser.getId(),
                authorities);
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.GET.name(),
                UrlConstants.USER_TERR_URL.formatted(savedUser.getId()));
        log.info("List with territory from database");
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_TERR_URL
                                .formatted(savedUser.getId()))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty());
        log.info("List with territory from Cache");
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_TERR_URL
                                .formatted(savedUser.getId()))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty());
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
