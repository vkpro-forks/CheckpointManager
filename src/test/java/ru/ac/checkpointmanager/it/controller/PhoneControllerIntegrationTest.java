package ru.ac.checkpointmanager.it.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.ac.checkpointmanager.config.RedisAndPostgresTestContainersConfiguration;
import ru.ac.checkpointmanager.config.security.WithMockCustomUser;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.model.Phone;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.repository.PhoneRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.security.CustomAuthenticationToken;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;
import ru.ac.checkpointmanager.utils.FieldsValidation;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Slf4j
public class PhoneControllerIntegrationTest extends RedisAndPostgresTestContainersConfiguration {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PhoneRepository phoneRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WebApplicationContext context;
    User savedUser;

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
        phoneRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void createPhoneNumberIsCreatedWithAdminRole() {
        UUID userId = savedUser.getId();
        PhoneDTO phoneDTO = TestUtils.getPhoneDto();
        phoneDTO.setUserId(userId);
        String phoneDtoString = TestUtils.jsonStringFromObject(phoneDTO);

        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PHONE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(phoneDtoString))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.number", Matchers.is(FieldsValidation.cleanPhone(phoneDTO.getNumber()))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.type", Matchers.is(phoneDTO.getType().name())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId", Matchers.is(phoneDTO.getUserId().toString())));
    }

    @Test
    @SneakyThrows
    void createPhoneNumberIsCreatedWithUserRoleAndRightId() {
        CustomAuthenticationToken token = TestUtils.getAuthToken(savedUser);
        UUID userId = savedUser.getId();
        PhoneDTO phoneDTO = TestUtils.getPhoneDto();
        phoneDTO.setUserId(userId);
        String phoneDtoString = TestUtils.jsonStringFromObject(phoneDTO);

        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PHONE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(token))
                        .content(phoneDtoString))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.number", Matchers.is(FieldsValidation.cleanPhone(phoneDTO.getNumber()))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.type", Matchers.is(phoneDTO.getType().name())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId", Matchers.is(phoneDTO.getUserId().toString())));
    }

    @Test
    @SneakyThrows
    void createPhoneNumberIsForbiddenWithUserRoleAndWrongId() {
        CustomAuthenticationToken token = TestUtils.getAuthToken(savedUser);
        UUID userId = UUID.randomUUID();
        PhoneDTO phoneDTO = TestUtils.getPhoneDto();
        phoneDTO.setUserId(userId);
        String phoneDtoString = TestUtils.jsonStringFromObject(phoneDTO);

        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PHONE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(token))
                        .content(phoneDtoString))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void createPhoneNumberIsConflict() {
        UUID userId = savedUser.getId();
        Phone phone = TestUtils.getPhoneForDB();
        phone.setUser(savedUser);
        phone.setNumber(FieldsValidation.cleanPhone(phone.getNumber()));
        Phone savedPhone = phoneRepository.saveAndFlush(phone);
        PhoneDTO phoneDTO = TestUtils.getPhoneDto();
        phoneDTO.setNumber(savedPhone.getNumber());
        phoneDTO.setUserId(userId);
        String phoneDtoString = TestUtils.jsonStringFromObject(phoneDTO);

        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PHONE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(phoneDtoString))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.is("Phone number already exists")));
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void createPhoneNumberIsNotFound() {
        UUID userId = UUID.randomUUID();
        PhoneDTO phoneDTO = TestUtils.getPhoneDto();
        phoneDTO.setUserId(userId);
        String phoneDtoString = TestUtils.jsonStringFromObject(phoneDTO);

        mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PHONE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(phoneDtoString))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.is("Object not found")));
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void findByIdIsOkWithRoleAdmin() {
        Phone phone = TestUtils.getPhoneForDB();
        phone.setUser(savedUser);
        phone.setNumber(FieldsValidation.cleanPhone(phone.getNumber()));
        Phone savedPhone = phoneRepository.saveAndFlush(phone);
        UUID phoneId = savedPhone.getId();

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.PHONE_URL + "/{id}", phoneId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(phoneId.toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.number", Matchers.is(savedPhone.getNumber())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.type", Matchers.is(savedPhone.getType().name())));
    }

    @Test
    @SneakyThrows
    void findByIdIsOkWithRoleUserAndRightId() {
        CustomAuthenticationToken token = TestUtils.getAuthToken(savedUser);
        Phone phone = TestUtils.getPhoneForDB();
        phone.setUser(savedUser);
        phone.setNumber(FieldsValidation.cleanPhone(phone.getNumber()));
        Phone savedPhone = phoneRepository.saveAndFlush(phone);
        UUID phoneId = savedPhone.getId();

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.PHONE_URL + "/{id}", phoneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(token)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", Matchers.is(phoneId.toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.number", Matchers.is(savedPhone.getNumber())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.type", Matchers.is(savedPhone.getType().name())));
    }

    @Test
    @SneakyThrows
    void findByIdIsForbiddenWithRoleUserAndWrongId() {
        CustomAuthenticationToken token = TestUtils.getAuthToken(savedUser);
        Phone phone = TestUtils.getPhoneForDB();
        User anotherUser = TestUtils.getUser();
        User savedAnotherUser = userRepository.saveAndFlush(anotherUser);
        phone.setUser(savedAnotherUser);
        phone.setNumber(FieldsValidation.cleanPhone(phone.getNumber()));
        Phone savedPhone = phoneRepository.saveAndFlush(phone);
        UUID phoneId = savedPhone.getId();

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.PHONE_URL + "/{id}", phoneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(token)))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void findByIdIsNotFound() {
        UUID phoneId = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.PHONE_URL + "/{id}", phoneId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.is("Object not found")));
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void getAllisOk() {
        Phone phone = TestUtils.getPhoneForDB();
        phone.setUser(savedUser);
        Phone savedPhone = phoneRepository.saveAndFlush(phone);

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.PHONE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.length()", Matchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].id", Matchers.hasItem(savedPhone.getId().toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].number", Matchers.hasItem(savedPhone.getNumber())))
                .andExpect(MockMvcResultMatchers.jsonPath("$[*].type", Matchers.hasItem(savedPhone.getType().name())));
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void getAllisOkWithoutNumbersInDB() {
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.PHONE_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.length()", Matchers.is(0)));
    }


    @Test
    @SneakyThrows
    @WithMockCustomUser
    void updateNumberIsOkWithAdminRole() {
        Phone phone = TestUtils.getPhoneForDB();
        phone.setUser(savedUser);
        phone.setNumber(FieldsValidation.cleanPhone(phone.getNumber()));
        Phone savedPhone = phoneRepository.saveAndFlush(phone);
        PhoneDTO phoneDTO = TestUtils.getPhoneDto();
        phoneDTO.setUserId(savedUser.getId());
        phoneDTO.setId(savedPhone.getId());
        phoneDTO.setNumber("79536575222");
        String phoneDtoString = TestUtils.jsonStringFromObject(phoneDTO);

        mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.PHONE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(phoneDtoString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(phoneDTO.getId().toString())))
                .andExpect(jsonPath("$.number", Matchers.is(FieldsValidation.cleanPhone(phoneDTO.getNumber()))))
                .andExpect(jsonPath("$.type", Matchers.is(phoneDTO.getType().name())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId", Matchers.is(phoneDTO.getUserId().toString())));
    }

    @Test
    @SneakyThrows
    void updateNumberIsOkWithUserRoleAndRightId() {
        CustomAuthenticationToken token = TestUtils.getAuthToken(savedUser);
        Phone phone = TestUtils.getPhoneForDB();
        phone.setUser(savedUser);
        phone.setNumber(FieldsValidation.cleanPhone(phone.getNumber()));
        Phone savedPhone = phoneRepository.saveAndFlush(phone);
        PhoneDTO phoneDTO = TestUtils.getPhoneDto();
        phoneDTO.setUserId(savedUser.getId());
        phoneDTO.setId(savedPhone.getId());
        phoneDTO.setNumber("79536575222");
        String phoneDtoString = TestUtils.jsonStringFromObject(phoneDTO);

        mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.PHONE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(token))
                        .content(phoneDtoString))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(phoneDTO.getId().toString())))
                .andExpect(jsonPath("$.number", Matchers.is(FieldsValidation.cleanPhone(phoneDTO.getNumber()))))
                .andExpect(jsonPath("$.type", Matchers.is(phoneDTO.getType().name())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId", Matchers.is(phoneDTO.getUserId().toString())));
    }

    @Test
    @SneakyThrows
    void updateNumberIsForbiddenWithUserRoleAndWrongId() {
        CustomAuthenticationToken token = TestUtils.getAuthToken(savedUser);
        User savedAnotherUser = userRepository.saveAndFlush(TestUtils.getUser());
        Phone phone = TestUtils.getPhoneForDB();
        phone.setUser(savedAnotherUser);
        phone.setNumber(FieldsValidation.cleanPhone(phone.getNumber()));
        Phone savedPhone = phoneRepository.saveAndFlush(phone);
        PhoneDTO phoneDTO = TestUtils.getPhoneDto();
        phoneDTO.setUserId(savedAnotherUser.getId());
        phoneDTO.setId(savedPhone.getId());
        phoneDTO.setNumber("79536575222");
        String phoneDtoString = TestUtils.jsonStringFromObject(phoneDTO);

        mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.PHONE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(token))
                        .content(phoneDtoString))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void updateNumberIsConflict() {
        UUID userId = savedUser.getId();
        Phone phone = TestUtils.getPhoneForDB();
        phone.setUser(savedUser);
        phone.setNumber(FieldsValidation.cleanPhone(phone.getNumber()));
        Phone savedPhone = phoneRepository.saveAndFlush(phone);
        PhoneDTO phoneDTO = TestUtils.getPhoneDto();
        phoneDTO.setId(savedPhone.getId());
        phoneDTO.setNumber(savedPhone.getNumber());
        phoneDTO.setUserId(userId);
        String phoneDtoString = TestUtils.jsonStringFromObject(phoneDTO);

        mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.PHONE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(phoneDtoString))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.is("Phone number already exists")));
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void updateNumberIsNotFound() {
        PhoneDTO phoneDTO = TestUtils.getPhoneDto();
        String phoneDtoString = TestUtils.jsonStringFromObject(phoneDTO);

        mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.PHONE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(phoneDtoString))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.is("Object not found")));
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void deleteNumberIsNoContentWithRoleAdmin() {
        Phone phone = TestUtils.getPhoneForDB();
        phone.setUser(savedUser);
        phone.setNumber(FieldsValidation.cleanPhone(phone.getNumber()));
        Phone savedPhone = phoneRepository.saveAndFlush(phone);
        UUID phoneId = savedPhone.getId();

        mockMvc.perform(MockMvcRequestBuilders.delete(UrlConstants.PHONE_URL + "/{id}", phoneId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @SneakyThrows
    void deleteNumberIsNoContentWithRoleUserWithRightId() {
        CustomAuthenticationToken token = TestUtils.getAuthToken(savedUser);
        Phone phone = TestUtils.getPhoneForDB();
        phone.setUser(savedUser);
        phone.setNumber(FieldsValidation.cleanPhone(phone.getNumber()));
        Phone savedPhone = phoneRepository.saveAndFlush(phone);
        UUID phoneId = savedPhone.getId();

        mockMvc.perform(MockMvcRequestBuilders.delete(UrlConstants.PHONE_URL + "/{id}", phoneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(token)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @SneakyThrows
    void deleteNumberIsForbiddenWithRoleUserWithWrongId() {
        CustomAuthenticationToken token = TestUtils.getAuthToken(savedUser);
        User userUsedToKick = userRepository.saveAndFlush(TestUtils.getUser());
        Phone phone = TestUtils.getPhoneForDB();
        phone.setUser(userUsedToKick);
        phone.setNumber(FieldsValidation.cleanPhone(phone.getNumber()));
        Phone savedPhone = phoneRepository.saveAndFlush(phone);
        UUID phoneId = savedPhone.getId();

        mockMvc.perform(MockMvcRequestBuilders.delete(UrlConstants.PHONE_URL + "/{id}", phoneId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(token)))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @SneakyThrows
    @WithMockCustomUser
    void deleteNumberIsNotFound() {
        UUID phoneId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.delete(UrlConstants.PHONE_URL + "/{id}", phoneId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.is("Object not found")));
    }
}
