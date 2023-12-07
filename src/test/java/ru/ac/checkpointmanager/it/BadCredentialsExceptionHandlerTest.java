package ru.ac.checkpointmanager.it;

import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.exception.handler.ErrorCode;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

class BadCredentialsExceptionHandlerTest extends GlobalExceptionHandlerBasicTestConfig {

    @Test
    @SneakyThrows
    void shouldHandleBadCredentialsExceptionForLogin() {
        String loginDto = TestUtils.jsonStringFromObject(TestUtils.getAuthenticationRequest());
        mockMvc.perform(MockMvcRequestBuilders
                        .post(UrlConstants.AUTH_LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginDto))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith("Bad")))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.UNAUTHORIZED.toString()));
    }

}
