package ru.ac.checkpointmanager.it;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.security.CustomAuthenticationToken;
import ru.ac.checkpointmanager.util.TestMessage;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.Collection;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@ActiveProfiles("test")
@Testcontainers
@Slf4j
class CrashedRedisIntegrationTest {

    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TerritoryRepository territoryRepository;

    @Autowired
    WebApplicationContext context;

    User savedUser;

    @Container
    @ServiceConnection(type = JdbcConnectionDetails.class)
    private static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("chpmanDB");

    @DynamicPropertySource
    private static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.liquibase.enabled", () -> true);
        registry.add("spring.liquibase.label-filter", () -> "!demo-data");
    }

    @BeforeEach
    void init() {
        User user = TestUtils.getUser();
        user.setRole(Role.ADMIN);
        savedUser = userRepository.saveAndFlush(user);
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @AfterEach
    void clear() {
        userRepository.deleteAll();
        territoryRepository.deleteAll();
    }

    @Test
    @SneakyThrows
    void getTerritories_CacheConnectionFailed_NoExceptionGetFromDB() {
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        territory.setUsers(List.of(savedUser));
        territoryRepository.saveAndFlush(territory);
        Collection<? extends GrantedAuthority> authorities = List
                .of(new SimpleGrantedAuthority(savedUser.getRole().name()));
        CustomAuthenticationToken authToken =
                new CustomAuthenticationToken(savedUser, null, savedUser.getId(), authorities);
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.GET.name(),
                UrlConstants.USER_TERR_URL.formatted(savedUser.getId()));
        log.info("List with territory from database");
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_TERR_URL
                                .formatted(savedUser.getId()))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty());
        log.info("List with territory again from DB");

        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_TERR_URL
                                .formatted(savedUser.getId()))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authToken)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isNotEmpty());
    }

}
