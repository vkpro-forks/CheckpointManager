package ru.ac.checkpointmanager.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.assertion.ResultActionsAssert;
import ru.ac.checkpointmanager.exception.handler.ErrorCode;
import ru.ac.checkpointmanager.exception.handler.ErrorMessage;

@Slf4j
public class ResultCheckUtils {

    private ResultCheckUtils() {
        throw new AssertionError("no instances");
    }

    public static void verifyUserResponseDTO(ResultActions resultActions, String id, String fullName, String mainNumber) throws Exception {
        log.info("Verifying UserResponseDTO");
        ResultActionsAssert.assertThat(resultActions).idMatches(id).fullNameMatches(fullName).mainNumberMatches(mainNumber)
                .contentTypeIsAppJson();
    }

    public static void checkWrongTypeFields(ResultActions resultActions) throws Exception {
        resultActions.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_TITLE)
                        .value(ErrorMessage.WRONG_ARGUMENT_PASSED))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.BAD_REQUEST.toString()));
    }

    public static void checkMissingRequestParamFields(ResultActions resultActions) throws Exception {
        resultActions.andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_TITLE)
                        .value(ErrorMessage.MISSING_REQUEST_PARAM))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.BAD_REQUEST.toString()));
    }

    public static void checkCommonValidationFields(ResultActions resultActions) throws Exception {
        resultActions.andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.VALIDATION.toString()));
    }

    public static void checkNotFoundFields(ResultActions resultActions) throws Exception {
        resultActions.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.NOT_FOUND.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_TIMESTAMP).isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_TITLE).isNotEmpty());
    }

    public static void checkForbiddenFields(ResultActions resultActions) throws Exception {
        resultActions.andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.FORBIDDEN.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_TIMESTAMP).isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_TITLE).isNotEmpty());
    }

    public static void checkValidationField(ResultActions resultActions, int index, String field) throws Exception {
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(index))
                .value(field));
    }
}
