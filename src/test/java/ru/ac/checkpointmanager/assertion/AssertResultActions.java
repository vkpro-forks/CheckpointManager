package ru.ac.checkpointmanager.assertion;

import org.assertj.core.api.AbstractAssert;
import org.hamcrest.Matchers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class AssertResultActions extends AbstractAssert<AssertResultActions, ResultActions> {

    public static final String JSON_ID = "$.id";

    public static final String JSON_FULL_NAME = "$.fullName";

    public static final String JSON_MAIN_NUMBER = "$.mainNumber";

    protected AssertResultActions(ResultActions resultActions) {
        super(resultActions, AssertResultActions.class);
    }

    public AssertResultActions contentTypeIsAppJson() throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON));
        return this;
    }

    public AssertResultActions idMatches(String id) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.is(id)));
        return this;
    }

    public AssertResultActions fullNameMatches(String fullName) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_FULL_NAME, Matchers.is(fullName)));
        return this;
    }

    public AssertResultActions mainNumberMatches(String mainNumber) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_MAIN_NUMBER, Matchers.is(mainNumber)));
        return this;
    }

    public static AssertResultActions assertThat(ResultActions resultActions) {
        return new AssertResultActions(resultActions);
    }
}
