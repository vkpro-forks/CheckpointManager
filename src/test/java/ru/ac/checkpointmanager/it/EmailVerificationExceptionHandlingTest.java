package ru.ac.checkpointmanager.it;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.exception.handler.ErrorCode;
import ru.ac.checkpointmanager.util.TestMessage;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

@Slf4j
class EmailVerificationExceptionHandlingTest extends GlobalExceptionHandlerBasicTestConfig {

    @Test
    @SneakyThrows
    void shouldHandleEmailVerificationTokenExceptionForConfirmRegistration() {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.GET.name(), UrlConstants.CONFIRM_REG_URL);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get(UrlConstants.CONFIRM_REG_URL)
                        .param("token", TestUtils.EMAIL_STRING_TOKEN))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.BAD_REQUEST.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_TITLE)
                        .value(Matchers.startsWith("Verification")));
    }

    /*@Test
    @SneakyThrows
    void shouldHandleUserNotFoundExceptionForConfirmEmail() {
        Cache emailCache = cacheManager.getCache("email");
        ConfirmChangeEmail changeEmail = TestUtils.getConfirmChangeEmail();
        assert emailCache != null;
        emailCache.put(changeEmail.getVerifiedToken(), changeEmail);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                        .get(UrlConstants.CONFIRM_EMAIL_URL)
                        .param("token", changeEmail.getVerifiedToken()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                        .value(Matchers.startsWith(USER)));
        TestUtils.checkNotFoundFields(resultActions);
    }*/

}
