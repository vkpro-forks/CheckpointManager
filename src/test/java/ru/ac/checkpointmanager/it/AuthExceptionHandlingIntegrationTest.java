package ru.ac.checkpointmanager.it;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.ac.checkpointmanager.config.CorsTestConfiguration;
import ru.ac.checkpointmanager.config.OpenAllEndpointsTestConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import({CorsTestConfiguration.class, OpenAllEndpointsTestConfiguration.class})
@ActiveProfiles("test")
public class AuthExceptionHandlingIntegrationTest {





}
