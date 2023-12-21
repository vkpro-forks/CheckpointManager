package ru.ac.checkpointmanager.it;

import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.dto.user.RefreshTokenDTO;
import ru.ac.checkpointmanager.exception.handler.ErrorCode;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.List;
import java.util.stream.Stream;

class UnauthorizedErrorsHandlerTest extends GlobalExceptionHandlerBasicTestConfig {

    @Test
    @SneakyThrows
    void shouldHandleBadCredentialsExceptionForLogin() {
        String loginDto = TestUtils.jsonStringFromObject(TestUtils.getAuthenticationRequest());
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .post(UrlConstants.AUTH_LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginDto))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith("Bad")));
        checkUnauthorized(resultActions);
    }

    @ParameterizedTest
    @MethodSource("getBadRefreshTokens")
    @SneakyThrows
    void shouldHandleInvalidTokenExceptionForRefreshToken(RefreshTokenDTO refreshTokenDTO) {
        String refreshTokenDto = TestUtils.jsonStringFromObject(refreshTokenDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .post(UrlConstants.AUTH_REFRESH_TOKEN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshTokenDto))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.containsStringIgnoringCase("JWT")));
        checkUnauthorized(resultActions);
    }

    private void checkUnauthorized(ResultActions resultActions) throws Exception {
        resultActions.andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.UNAUTHORIZED.toString()));
    }

    private static Stream<RefreshTokenDTO> getBadRefreshTokens() {
        List<String> roles = List.of("ROLE_ADMIN");
        RefreshTokenDTO expired = new RefreshTokenDTO(TestUtils.getJwt(-1, "name", roles, true, true));
        RefreshTokenDTO nullName = new RefreshTokenDTO(TestUtils.getJwt(1000000, null, roles, true, true));
        RefreshTokenDTO emptyName = new RefreshTokenDTO(TestUtils.getJwt(1000000, "", roles, true, true));
        String goodToken = TestUtils.getRefreshTokenDTO().getRefreshToken();
        RefreshTokenDTO badJwt = new RefreshTokenDTO(goodToken + 1);
        RefreshTokenDTO notRefresh = new RefreshTokenDTO(TestUtils.getJwt(1000000, "", roles, false, true));
        RefreshTokenDTO withoutIdClaim = new RefreshTokenDTO(TestUtils.getJwt(1000000, "", roles, true, false));
        return Stream.of(expired, nullName, emptyName, badJwt, notRefresh, withoutIdClaim);
    }


}
