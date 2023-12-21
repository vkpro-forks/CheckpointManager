package ru.ac.checkpointmanager.it;

import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.CorsTestConfiguration;
import ru.ac.checkpointmanager.config.PostgresTestContainersConfiguration;
import ru.ac.checkpointmanager.exception.handler.ErrorCode;
import ru.ac.checkpointmanager.service.user.UserService;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import({CorsTestConfiguration.class})
@ActiveProfiles("test")
class SecurityFilterIntegrationTest extends PostgresTestContainersConfiguration {

    @MockBean
    UserService userService;

    @Autowired
    MockMvc mockMvc;

    @Test
    @SneakyThrows
    void shouldReturn403ErrorIfNotJwtInBearerFound() {
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL)
                        .header(TestUtils.AUTH_HEADER, ""))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.FORBIDDEN.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWithIgnoringCase("Jwt is not present")));
    }

    @Test
    @SneakyThrows
    void shouldReturn403ErrorIfJwtIsBad() {
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL)
                        .header(TestUtils.AUTH_HEADER, TestUtils.BEARER + "Bearer"))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.FORBIDDEN.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWithIgnoringCase("Jwt is invalid")));
    }

    @Test
    @SneakyThrows
    void shouldReturnJwtExpiredErrorIfAccessTokenExpired() {
        String jwt = TestUtils.getJwt(-1, TestUtils.USERNAME, List.of("ADMIN"), false, true);
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL)
                        .header(TestUtils.AUTH_HEADER, TestUtils.BEARER + jwt))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.TOKEN_EXPIRED.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWithIgnoringCase("JWT expired")));
    }

    @Test
    @SneakyThrows
    void shouldReturn401IfUserFromJwtNotExists() {
        String jwt = TestUtils.getJwt(600000, TestUtils.USERNAME, List.of("ADMIN"), false, true);
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.USER_URL)
                        .header(TestUtils.AUTH_HEADER, TestUtils.BEARER + jwt))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.UNAUTHORIZED.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWithIgnoringCase("User")));
    }

}
