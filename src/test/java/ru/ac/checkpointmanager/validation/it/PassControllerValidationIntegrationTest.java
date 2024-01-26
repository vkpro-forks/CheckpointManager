package ru.ac.checkpointmanager.validation.it;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.OpenAllEndpointsTestConfiguration;
import ru.ac.checkpointmanager.config.ValidationTestConfiguration;
import ru.ac.checkpointmanager.controller.PassController;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.mapper.PassMapper;
import ru.ac.checkpointmanager.service.passes.PassService;
import ru.ac.checkpointmanager.util.ResultCheckUtils;
import ru.ac.checkpointmanager.util.TestMessage;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.time.LocalDateTime;
import java.util.stream.Stream;

@WebMvcTest(PassController.class)
@Import({ValidationTestConfiguration.class, OpenAllEndpointsTestConfiguration.class})
@WithMockUser(roles = {"ADMIN"})
@ActiveProfiles("test")
@Slf4j
class PassControllerValidationIntegrationTest {

    private static final String CAR = "car";

    private static final String VISITOR = "visitor";

    private static final String START_TIME = "startTime";

    private static final String END_TIME = "endTime";
    public static final String NAME = "visitor.name";
    public static final String PHONE = "visitor.phone";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PassService passService;

    @MockBean
    PassMapper passMapper;

    @Test
    @SneakyThrows
    void shouldReturnValidationErrorForNullCarAndVisitorFieldsForAddPass() {
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        passCreateDTO.setCar(null);
        passCreateDTO.setVisitor(null);
        String passDtoCreateString = TestUtils.jsonStringFromObject(passCreateDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passDtoCreateString));
        checkCarOrVisitorFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldReturnValidationErrorForBothCarAndVisitorFieldsForAddPass() {
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        passCreateDTO.setCar(new CarDTO());
        passCreateDTO.setVisitor(new VisitorDTO());
        String passDtoCreateString = TestUtils.jsonStringFromObject(passCreateDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passDtoCreateString));
        checkCarOrVisitorFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldReturnValidationErrorForNullCarAndVisitorFieldsForUpdatePass() {
        PassUpdateDTO passUpdateDTO = TestUtils.getPassUpdateDTO();
        passUpdateDTO.setCar(null);
        passUpdateDTO.setVisitor(null);
        String passDtoCreateString = TestUtils.jsonStringFromObject(passUpdateDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.PASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passDtoCreateString));
        checkCarOrVisitorFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldReturnValidationErrorForBothCarAndVisitorFieldsForUpdatePass() {
        PassUpdateDTO passUpdateDTO = TestUtils.getPassUpdateDTO();
        passUpdateDTO.setCar(new CarDTO());
        passUpdateDTO.setVisitor(new VisitorDTO());
        String passDtoCreateString = TestUtils.jsonStringFromObject(passUpdateDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.PASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passDtoCreateString));
        checkCarOrVisitorFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldReturnValidationErrorIncorrectStartAndEndTimeFieldsForAddPass() {
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        passCreateDTO.setCar(new CarDTO());
        passCreateDTO.setEndTime(LocalDateTime.now().plusHours(1));
        passCreateDTO.setStartTime(LocalDateTime.now().plusHours(3));
        String passDtoCreateString = TestUtils.jsonStringFromObject(passCreateDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passDtoCreateString));
        checkStartEndTimeFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldReturnValidationErrorIncorrectStartAndEndTimeFieldsForUpdatePass() {
        PassUpdateDTO passUpdateDTO = TestUtils.getPassUpdateDTO();
        passUpdateDTO.setCar(new CarDTO());
        passUpdateDTO.setEndTime(LocalDateTime.now().plusHours(1));
        passUpdateDTO.setStartTime(LocalDateTime.now().plusHours(3));
        String passDtoUpdateString = TestUtils.jsonStringFromObject(passUpdateDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.PASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passDtoUpdateString));
        checkStartEndTimeFields(resultActions);
    }

    @ParameterizedTest
    @MethodSource("getGoodPassTimeAndNull")
    @SneakyThrows
    void shouldReturnValidationErrorIfStartOrEndTimeOfPassIsNullInPassCreateDTO(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Creating dto with one good date and one null");
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        passCreateDTO.setCar(new CarDTO());
        passCreateDTO.setEndTime(startTime);
        passCreateDTO.setStartTime(endTime);
        String passDtoCreateString = TestUtils.jsonStringFromObject(passCreateDTO);
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST, UrlConstants.PASS_URL);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passDtoCreateString));
        ResultCheckUtils.checkCommonValidationFields(resultActions);
    }

    @ParameterizedTest
    @MethodSource("getGoodPassTimeAndNull")
    @SneakyThrows
    void shouldReturnValidationErrorIfStartOrEndTimeOfPassIsNullInPassUpdateDTO(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Creating dto with one good date and one null");
        PassUpdateDTO passUpdateDTO = TestUtils.getPassUpdateDTO();
        passUpdateDTO.setCar(new CarDTO());
        passUpdateDTO.setEndTime(startTime);
        passUpdateDTO.setStartTime(endTime);
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.PUT, UrlConstants.PASS_URL);
        String passDtoUpdateString = TestUtils.jsonStringFromObject(passUpdateDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.PASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passDtoUpdateString));
        ResultCheckUtils.checkCommonValidationFields(resultActions);
    }

    @ParameterizedTest
    @MethodSource("getBadVisitorDto")
    @SneakyThrows
    void addPass_VisitorHasBadField_HandleExceptionAndReturnValidationError(String name, String phone, String field) {
        log.info("Creating dto with one good date and one null");
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithVisitor();
        passCreateDTO.getVisitor().setName(name);
        passCreateDTO.getVisitor().setPhone(phone);
        String passDtoCreateString = TestUtils.jsonStringFromObject(passCreateDTO);
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST, UrlConstants.PASS_URL);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passDtoCreateString));

        ResultCheckUtils.checkCommonValidationFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0))
                .value(field));
    }

    private static void checkCarOrVisitorFields(ResultActions resultActions) throws Exception {
        ResultCheckUtils.checkCommonValidationFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0))
                        .value(Matchers.anyOf(Matchers.is(CAR), Matchers.is(VISITOR))))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(1))
                        .value(Matchers.anyOf(Matchers.is(CAR), Matchers.is(VISITOR))));
    }

    private static void checkStartEndTimeFields(ResultActions resultActions) throws Exception {
        ResultCheckUtils.checkCommonValidationFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0))
                        .value(Matchers.anyOf(Matchers.is(START_TIME), Matchers.is(END_TIME))))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(1))
                        .value(Matchers.anyOf(Matchers.is(START_TIME), Matchers.is(END_TIME))));
    }

    private static Stream<Arguments> getGoodPassTimeAndNull() {
        return Stream.of(
                Arguments.of(LocalDateTime.now().plusHours(1), null),
                Arguments.of(null, LocalDateTime.now().plusHours(1))
        );
    }

    private static Stream<Arguments> getBadVisitorDto() {
        return Stream.of(Arguments.of("", TestUtils.PHONE_NUM, NAME),
                Arguments.of(null, TestUtils.PHONE_NUM, NAME),
                Arguments.of(TestUtils.FULL_NAME, "", PHONE),
                Arguments.of(TestUtils.FULL_NAME, "a".repeat(10), PHONE),
                Arguments.of(TestUtils.FULL_NAME, "a".repeat(22), PHONE)
        );
    }

}
