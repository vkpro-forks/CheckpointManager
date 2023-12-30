package ru.ac.checkpointmanager.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
import ru.ac.checkpointmanager.config.CacheTestConfiguration;
import ru.ac.checkpointmanager.config.PostgresTestContainersConfiguration;
import ru.ac.checkpointmanager.config.security.WithMockCustomUser;
import ru.ac.checkpointmanager.dto.user.ChangePasswordRequest;
import ru.ac.checkpointmanager.dto.user.UserPutDTO;
import ru.ac.checkpointmanager.model.Phone;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
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
@Import(CacheTestConfiguration.class)
@ActiveProfiles("test")
@Slf4j
class UserControllerIntegrationTest extends PostgresTestContainersConfiguration {

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

    private CustomAuthenticationToken getAuthToken(User user) { // мб в утилсы перекинуть?
        Collection<? extends GrantedAuthority> authorities = List
                .of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        return new CustomAuthenticationToken(user, null, user.getId(), authorities);
    }

    @Test
    @SneakyThrows
    void findUserByIdIsOK() {
        UUID userId = savedUser.getId();
        CustomAuthenticationToken authToken = getAuthToken(savedUser);

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
        CustomAuthenticationToken authToken = getAuthToken(savedUser);
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
        CustomAuthenticationToken authToken = getAuthToken(savedUser);
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
        CustomAuthenticationToken authToken = getAuthToken(savedUser);
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
        CustomAuthenticationToken authToken = getAuthToken(savedUser);
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
        CustomAuthenticationToken authToken = getAuthToken(savedUser);
        UUID userId = savedUser.getId();
        UserPutDTO userPutDTO = TestUtils.getUserPutDTO();
        userPutDTO.setId(userId);
        userPutDTO.setMainNumber(FieldsValidation.cleanPhone(userPutDTO.getMainNumber()));
        String userPutDTOString = TestUtils.jsonStringFromObject(userPutDTO);

        mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.USER_URL)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken))
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
        UUID userId = savedUser.getId();
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
    void updateUserIsForbiddenWithWrongUserId() {
        CustomAuthenticationToken authToken = getAuthToken(savedUser);
        UUID userId = UUID.randomUUID();
        UserPutDTO userPutDTO = TestUtils.getUserPutDTO();
        userPutDTO.setId(userId);
        String userPutDTOString = TestUtils.jsonStringFromObject(userPutDTO);

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
    void changePasswordIsNoContent() {
        CustomAuthenticationToken authToken = getAuthToken(savedUser);
        ChangePasswordRequest request = TestUtils.getChangePasswordRequest();
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
}
