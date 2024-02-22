package ru.ac.checkpointmanager.assertion;

import org.assertj.core.api.AbstractAssert;
import org.hamcrest.Matchers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class ResultActionsAssert extends AbstractAssert<ResultActionsAssert, ResultActions> {

    public static final String JSON_ID = "$.id";

    public static final String JSON_FULL_NAME = "$.fullName";

    public static final String JSON_MAIN_NUMBER = "$.mainNumber";

    protected ResultActionsAssert(ResultActions resultActions) {
        super(resultActions, ResultActionsAssert.class);
    }

    public ResultActionsAssert contentTypeIsAppJson() throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        return this;
    }

    public ResultActionsAssert idMatches(String id) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.is(id)));
        return this;
    }

    public ResultActionsAssert fullNameMatches(String fullName) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_FULL_NAME, Matchers.is(fullName)));
        return this;
    }

    public ResultActionsAssert mainNumberMatches(String mainNumber) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_MAIN_NUMBER, Matchers.is(mainNumber)));
        return this;
    }




    public static ResultActionsAssert assertThat(ResultActions resultActions) {
        return new ResultActionsAssert(resultActions);
    }
}
