package ru.ac.checkpointmanager.assertion;

import org.hamcrest.Matchers;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.model.passes.PassTimeType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AssertPassResultActions extends AssertResultActions {
    public static final String JSON_COMMENT = "$.comment";

    public static final String JSON_END_TIME = "$.endTime";

    public static final String JSON_START_TIME = "$.startTime";

    public static final String JSON_TIME_TYPE = "$.timeType";

    public static final String JSON_TIME_TYPE_DESC = "$.timeTypeDescription";

    public static final String JSON_CAR_LICENSE_PLATE = "$.car.licensePlate";

    public static final String JSON_PASS_VISITOR_NAME = "$.visitor.name";

    public static final String JSON_PASS_VISITOR_PHONE = "$.visitor.phone";

    public static final String JSON_PASS_VISITOR_NOTE = "$.visitor.note";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static AssertPassResultActions assertThat(ResultActions resultActions) {
        return new AssertPassResultActions(resultActions);
    }

    protected AssertPassResultActions(ResultActions resultActions) {
        super(resultActions);
    }

    public AssertPassResultActions commentMatches(String comment) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_COMMENT, Matchers.equalTo(comment)));
        return this;
    }

    public AssertPassResultActions startDateMatches(LocalDateTime startTime) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_START_TIME, Matchers.startsWith(DATE_TIME_FORMATTER.format(startTime))));
        return this;
    }

    public AssertPassResultActions endDateMatches(LocalDateTime endTime) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_END_TIME, Matchers.startsWith(DATE_TIME_FORMATTER.format(endTime))));
        return this;
    }

    public AssertPassResultActions passCarLicensePlateMatches(String licensePlate) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_CAR_LICENSE_PLATE, Matchers.equalTo(licensePlate)));
        return this;
    }

    public AssertPassResultActions passTimeTypeMatches(PassTimeType passTimeType) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_TIME_TYPE, Matchers.equalTo(passTimeType.name())))
                .andExpect(MockMvcResultMatchers.jsonPath(JSON_TIME_TYPE_DESC,
                        Matchers.equalTo(passTimeType.getDescription())));
        return this;
    }

    public AssertPassResultActions passVisitorNameMatches(String name) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_PASS_VISITOR_NAME, Matchers.equalTo(name)));
        return this;
    }

    public AssertPassResultActions passVisitorPhoneMatches(String phone) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_PASS_VISITOR_PHONE, Matchers.equalTo(phone)));
        return this;
    }

    public AssertPassResultActions passVisitorNoteMatches(String note) throws Exception {
        isNotNull();
        actual.andExpect(MockMvcResultMatchers.jsonPath(JSON_PASS_VISITOR_NOTE, Matchers.equalTo(note)));
        return this;
    }
}
