package ru.ac.checkpointmanager.assertion;

import org.assertj.core.api.AbstractAssert;
import org.hamcrest.Matchers;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.model.passes.PassTimeType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AssertResultActions extends AbstractAssert<AssertResultActions, ResultActions> {

    public static final String JSON_ID = "$.id";

    public static final String JSON_FULL_NAME = "$.fullName";

    public static final String JSON_MAIN_NUMBER = "$.mainNumber";
    public static final String JSON_COMMENT = "$.comment";
    public static final String JSON_END_TIME = "$.endTime";

    public static final String JSON_START_TIME = "$.startTime";

    public static final String JSON_CAR_LICENSE_PLATE = "$.car.licensePlate";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    public static final String JSON_TIME_TYPE = "$.timeType";
    public static final String JSON_TIME_TYPE_DESC = "$.timeTypeDescription";
    public static final String JSON_PASS_VISITOR_NAME = "$.visitor.name";
    public static final String JSON_PASS_VISITOR_PHONE = "$.visitor.phone";
    public static final String JSON_PASS_VISITOR_NOTE = "$.visitor.note";

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

    public AssertResultActions commentMatches(String comment) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_COMMENT, Matchers.equalTo(comment)));
        return this;
    }

    public AssertResultActions startDateMatches(LocalDateTime startTime) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_START_TIME, Matchers.startsWith(DATE_TIME_FORMATTER.format(startTime))));
        return this;
    }

    public AssertResultActions endDateMatches(LocalDateTime endTime) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_END_TIME, Matchers.startsWith(DATE_TIME_FORMATTER.format(endTime))));
        return this;
    }

    public AssertResultActions passCarLicensePlateMatches(String licensePlate) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_CAR_LICENSE_PLATE, Matchers.equalTo(licensePlate)));
        return this;
    }

    public AssertResultActions passTimeTypeMatches(PassTimeType passTimeType) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_TIME_TYPE, Matchers.equalTo(passTimeType.name())))
                .andExpect(MockMvcResultMatchers.jsonPath(JSON_TIME_TYPE_DESC,
                        Matchers.equalTo(passTimeType.getDescription())));
        return this;
    }

    public AssertResultActions passVisitorNameMatches(String name) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_PASS_VISITOR_NAME, Matchers.equalTo(name)));
        return this;
    }

    public AssertResultActions passVisitorPhoneMatches(String phone) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_PASS_VISITOR_PHONE, Matchers.equalTo(phone)));
        return this;
    }

    public AssertResultActions passVisitorNoteMatches(String note) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_PASS_VISITOR_NOTE, Matchers.equalTo(note)));
        return this;
    }

    public static AssertResultActions assertThat(ResultActions resultActions) {
        return new AssertResultActions(resultActions);
    }
}
