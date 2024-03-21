package ru.ac.checkpointmanager.assertion;

import org.hamcrest.Matchers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.exception.handler.ErrorCode;

public class ErrorResponseResultActionsAssert extends ResultActionsAssert {

    public static final String JSON_TIMESTAMP = "$.timestamp";

    public static final String JSON_ERROR_CODE = "$.errorCode";

    public static final String JSON_TITLE = "$.title";

    public static final String JSON_DETAIL = "$.detail";

    public static ErrorResponseResultActionsAssert assertThat(ResultActions resultActions) {
        return new ErrorResponseResultActionsAssert(resultActions);
    }

    protected ErrorResponseResultActionsAssert(ResultActions resultActions) {
        super(resultActions);
    }

    public ErrorResponseResultActionsAssert errorCodeMatches(ErrorCode errorCode) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_ERROR_CODE, Matchers.equalTo(errorCode.toString())));
        return this;
    }

    public ErrorResponseResultActionsAssert timeStampNotEmpty() throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_TIMESTAMP).isNotEmpty());
        return this;
    }

    public ErrorResponseResultActionsAssert contentTypeIsApplicationProblemJson() throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
        return this;
    }

    public ErrorResponseResultActionsAssert titleMatches(String title) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_TITLE, Matchers.equalTo(title)));
        return this;
    }

    public ErrorResponseResultActionsAssert detailsMatches(String details) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_DETAIL, Matchers.equalTo(details)));
        return this;
    }

    public ErrorResponseResultActionsAssert detailsIsNotEmpty() throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_DETAIL).isNotEmpty());
        return this;
    }
}
