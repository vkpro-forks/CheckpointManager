package ru.ac.checkpointmanager.it;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.ac.checkpointmanager.config.RedisAndPostgresTestContainersConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = {"ADMIN"})
public class GlobalExceptionHandlerBasicTestConfig extends RedisAndPostgresTestContainersConfiguration {

    @Autowired
    MockMvc mockMvc;

}
