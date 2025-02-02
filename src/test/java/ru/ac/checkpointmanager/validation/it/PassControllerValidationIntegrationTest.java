package ru.ac.checkpointmanager.validation.it;

import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.OpenAllEndpointsTestConfiguration;
import ru.ac.checkpointmanager.config.ValidationTestConfiguration;
import ru.ac.checkpointmanager.controller.PassController;
import ru.ac.checkpointmanager.dto.CarBrandDTO;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.mapper.PassMapper;
import ru.ac.checkpointmanager.service.passes.PassService;
import ru.ac.checkpointmanager.util.MockMvcUtils;
import ru.ac.checkpointmanager.util.PassTestData;
import ru.ac.checkpointmanager.util.CheckResultActionsUtils;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

@WebMvcTest(PassController.class)
@Import({ValidationTestConfiguration.class, OpenAllEndpointsTestConfiguration.class})
@WithMockUser(roles = {"ADMIN"})
@ActiveProfiles("test")
class PassControllerValidationIntegrationTest {

    private static final String CAR = "car";

    private static final String VISITOR = "visitor";

    private static final String START_TIME = "startTime";

    private static final String END_TIME = "endTime";

    private static final String NAME = "visitor.name";

    private static final String PHONE = "visitor.phone";
    public static final String CAR_LICENSE_PLATE = "car.licensePlate";
    public static final String CAR_BRAND = "car.brand";

    public static final String CAR_BRAND_BRAND = "car.brand.brand";
    public static final String CAR_PHONE = "car.phone";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PassService passService;

    @MockBean
    PassMapper passMapper;

    @Test
    @SneakyThrows
    void addPass_NullCarAndVisitorFields_HandleExceptionAndReturnValidationError() { // testing CarOrVisitorFieldsCheck
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithCar();
        passCreateDTO.setCar(null);
        passCreateDTO.setVisitor(null);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        checkCarOrVisitorFields(resultActions);
    }

    @Test
    @SneakyThrows
    void addPass_BothCarAndVisitorFieldsForAddPass_HandleExceptionAndReturnValidationError() { // testing CarOrVisitorFieldsCheck
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithCar();
        passCreateDTO.setVisitor(TestUtils.getVisitorDTO());

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        checkCarOrVisitorFields(resultActions);
    }

    @Test
    @SneakyThrows
    void updatePass_NullCarAndVisitorFields_HandleExceptionAndReturnValidationError() { // testing CarOrVisitorFieldsCheck
        PassUpdateDTO passUpdateDTO = PassTestData.getPassUpdateDTOWithCar();
        passUpdateDTO.setCar(null);
        passUpdateDTO.setVisitor(null);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.updatePass(passUpdateDTO));

        checkCarOrVisitorFields(resultActions);
    }

    @Test
    @SneakyThrows
    void updatePass_BothCarAndVisitorFields_HandleExceptionAndReturnValidationError() { // testing CarOrVisitorFieldsCheck
        PassUpdateDTO passUpdateDTO = PassTestData.getPassUpdateDTOWithCar();
        passUpdateDTO.setVisitor(TestUtils.getVisitorDTO());

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.updatePass(passUpdateDTO));

        checkCarOrVisitorFields(resultActions);
    }

    @Test
    @SneakyThrows
    void addPass_IncorrectStartAndEndTimeFields_HandleExceptionAndReturnValidationError() { // testing PassTimeCheck
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithCar();
        passCreateDTO.setEndTime(LocalDateTime.now().plusHours(1));
        passCreateDTO.setStartTime(LocalDateTime.now().plusHours(3));

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        checkStartEndTimeFields(resultActions);
    }

    @Test
    @SneakyThrows
    void updatePass_IncorrectStartAndEndTimeFields_HandleExceptionAndReturnValidationError() { // testing PassTimeCheck
        PassUpdateDTO passUpdateDTO = PassTestData.getPassUpdateDTOWithCar();
        passUpdateDTO.setEndTime(LocalDateTime.now().plusHours(1));
        passUpdateDTO.setStartTime(LocalDateTime.now().plusHours(3));

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.updatePass(passUpdateDTO));

        checkStartEndTimeFields(resultActions);
    }

    @ParameterizedTest
    @MethodSource("getGoodPassTimeAndNull")
    @SneakyThrows
    void addPass_StartOrEndTimeOfPassIsNull_HandleExceptionAndReturnValidationError(
            LocalDateTime startTime, LocalDateTime endTime) { // testing NotNull
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithCar();
        passCreateDTO.setEndTime(startTime);
        passCreateDTO.setStartTime(endTime);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        CheckResultActionsUtils.checkCommonValidationFields(resultActions);
    }

    @ParameterizedTest
    @MethodSource("getGoodPassTimeAndNull")
    @SneakyThrows
    void updatePass_IfStartOrEndTimeOfPassIsNullInPassUpdateDTO_HandleExceptionAndReturnValidationError(
            LocalDateTime startTime, LocalDateTime endTime) { // testing NotNull
        PassUpdateDTO passUpdateDTO = PassTestData.getPassUpdateDTOWithCar();
        passUpdateDTO.setEndTime(startTime);
        passUpdateDTO.setStartTime(endTime);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.updatePass(passUpdateDTO));

        CheckResultActionsUtils.checkCommonValidationFields(resultActions);
    }

    @Test
    @SneakyThrows
    void addPass_EndTimeNotInFuture_HandleExceptionAndReturnValidationError() { // testing Future
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithCar();
        passCreateDTO.setEndTime(LocalDateTime.now().minusDays(1));

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        CheckResultActionsUtils.checkCommonValidationFields(resultActions);
        CheckResultActionsUtils.checkValidationField(resultActions, 0, END_TIME);
    }

    @Test
    @SneakyThrows
    void updatePass_EndTimeNotInFuture_HandleExceptionAndReturnValidationError() { // testing Future
        PassUpdateDTO passUpdateDTO = PassTestData.getPassUpdateDTOWithCar();
        passUpdateDTO.setEndTime(LocalDateTime.now().minusDays(1));

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.updatePass(passUpdateDTO));

        CheckResultActionsUtils.checkCommonValidationFields(resultActions);
        CheckResultActionsUtils.checkValidationField(resultActions, 0, END_TIME);
    }


    @ParameterizedTest
    @MethodSource("getBadVisitorDto")
    @SneakyThrows
    void addPass_VisitorHasBadField_HandleExceptionAndReturnValidationError(String name, String phone, String field) {
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithVisitor();
        assert passCreateDTO.getVisitor() != null;
        passCreateDTO.getVisitor().setName(name);
        passCreateDTO.getVisitor().setPhone(phone);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        CheckResultActionsUtils.checkCommonValidationFields(resultActions);
        CheckResultActionsUtils.checkValidationField(resultActions, 0, field);
    }

    @ParameterizedTest
    @MethodSource("getBadVisitorDto")
    @SneakyThrows
    void updatePass_VisitorHasBadField_HandleExceptionAndReturnValidationError(String name, String phone, String field) {
        PassUpdateDTO passUpdateDTO = PassTestData.getPassUpdateDTOVisitor();
        assert passUpdateDTO.getVisitor() != null;
        passUpdateDTO.getVisitor().setName(name);
        passUpdateDTO.getVisitor().setPhone(phone);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.updatePass(passUpdateDTO));

        CheckResultActionsUtils.checkCommonValidationFields(resultActions);
        CheckResultActionsUtils.checkValidationField(resultActions, 0, field);
    }

    @ParameterizedTest
    @MethodSource("getBadCarDto")
    @SneakyThrows
    void addPass_CarHasBadField_HandleExceptionAndReturnValidationError(String licensePlate, CarBrandDTO carBrandDTO,
                                                                        String phone, String field) {
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithCar();
        CarDTO car = passCreateDTO.getCar();
        assert car != null;
        car.setLicensePlate(licensePlate);
        car.setPhone(phone);
        car.setBrand(carBrandDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        CheckResultActionsUtils.checkCommonValidationFields(resultActions);
        CheckResultActionsUtils.checkValidationField(resultActions, 0, field);
    }

    @ParameterizedTest
    @MethodSource("getBadCarDto")
    @SneakyThrows
    void updatePass_CarHasBadField_HandleExceptionAndReturnValidationError(String licensePlate, CarBrandDTO carBrandDTO,
                                                                           String phone, String field) {
        PassUpdateDTO passUpdateDTO = PassTestData.getPassUpdateDTOWithCar();
        CarDTO car = passUpdateDTO.getCar();
        assert car != null;
        car.setLicensePlate(licensePlate);
        car.setPhone(phone);
        car.setBrand(carBrandDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.updatePass(passUpdateDTO));

        CheckResultActionsUtils.checkCommonValidationFields(resultActions);
        CheckResultActionsUtils.checkValidationField(resultActions, 0, field);
    }

    @ParameterizedTest
    @MethodSource("getNullIds")
    @SneakyThrows
    void addPass_NullUserIdOrTerritoryId_HandleExceptionAndReturnValidationError(UUID userId, UUID terrId, String field) {
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithVisitor();
        passCreateDTO.setTerritoryId(terrId);
        passCreateDTO.setUserId(userId);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        CheckResultActionsUtils.checkCommonValidationFields(resultActions);
        CheckResultActionsUtils.checkValidationField(resultActions, 0, field);
    }

    @Test
    @SneakyThrows
    void addPass_TooLongComment_HandleExceptionAndReturnValidationError() {
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithVisitor();
        passCreateDTO.setComment("a".repeat(31));

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        CheckResultActionsUtils.checkCommonValidationFields(resultActions);
        CheckResultActionsUtils.checkValidationField(resultActions, 0, "comment");
    }

    @ParameterizedTest
    @MethodSource("getMethodsWithBadUids")
    @SneakyThrows
    void allEndpoints_BadUUIDPassed_ReturnBadRequest(MockHttpServletRequestBuilder requestBuilder) {
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        CheckResultActionsUtils.checkWrongTypeFields(resultActions);
    }

    private static void checkCarOrVisitorFields(ResultActions resultActions) throws Exception {
        CheckResultActionsUtils.checkCommonValidationFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0))
                        .value(Matchers.anyOf(Matchers.is(CAR), Matchers.is(VISITOR))))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(1))
                        .value(Matchers.anyOf(Matchers.is(CAR), Matchers.is(VISITOR))));
    }

    private static void checkStartEndTimeFields(ResultActions resultActions) throws Exception {
        CheckResultActionsUtils.checkCommonValidationFields(resultActions);
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

    private static Stream<Arguments> getNullIds() {
        return Stream.of(
                Arguments.of(null, TestUtils.TERR_ID, "userId"),
                Arguments.of(TestUtils.USER_ID, null, "territoryId")
        );
    }

    private static Stream<Arguments> getBadCarDto() {
        CarBrandDTO carBrandDTO = TestUtils.getCarBrandDTO();
        CarBrandDTO carBrandWithNull = new CarBrandDTO();
        CarBrandDTO carBrandWithOneSymbol = new CarBrandDTO("a");
        CarBrandDTO carBrandWithTooLong = new CarBrandDTO("a".repeat(26));
        CarBrandDTO carBrandWithNoPatternField = new CarBrandDTO("*]123");
        return Stream.of(
                Arguments.of(null, carBrandDTO, TestUtils.PHONE_NUM, CAR_LICENSE_PLATE),
                Arguments.of("a".repeat(5), carBrandDTO, TestUtils.PHONE_NUM, CAR_LICENSE_PLATE),
                Arguments.of("a".repeat(11), carBrandDTO, TestUtils.PHONE_NUM, CAR_LICENSE_PLATE),
                Arguments.of("*kd]asdf", carBrandDTO, TestUtils.PHONE_NUM, CAR_LICENSE_PLATE),
                Arguments.of(TestUtils.LICENSE_PLATE, null, TestUtils.PHONE_NUM, CAR_BRAND),
                Arguments.of(TestUtils.LICENSE_PLATE, carBrandWithOneSymbol, TestUtils.PHONE_NUM, CAR_BRAND_BRAND),
                Arguments.of(TestUtils.LICENSE_PLATE, carBrandWithTooLong, TestUtils.PHONE_NUM, CAR_BRAND_BRAND),
                Arguments.of(TestUtils.LICENSE_PLATE, carBrandWithNull, TestUtils.PHONE_NUM, CAR_BRAND_BRAND),
                Arguments.of(TestUtils.LICENSE_PLATE, carBrandWithNoPatternField, TestUtils.PHONE_NUM, CAR_BRAND_BRAND),
                Arguments.of(TestUtils.LICENSE_PLATE, carBrandDTO, "a".repeat(10), CAR_PHONE),
                Arguments.of(TestUtils.LICENSE_PLATE, carBrandDTO, "a".repeat(22), CAR_PHONE),
                Arguments.of(TestUtils.LICENSE_PLATE, carBrandDTO, "asdfasdfasdfasdf", CAR_PHONE)
        );
    }

    private static Stream<MockHttpServletRequestBuilder> getMethodsWithBadUids() {
        return Stream.of(
                MockMvcRequestBuilders.get(UrlConstants.PASS_URL + "/{passId}", "notUUD"),
                MockMvcRequestBuilders.get(UrlConstants.PASS_USER_URL, "notUUD"),
                MockMvcRequestBuilders.get(UrlConstants.PASS_URL_TERRITORY, "not UUID"),
                MockMvcRequestBuilders.get(UrlConstants.PASS_USER_TERRITORIES_URL, "notUUD"),
                MockMvcRequestBuilders.patch(UrlConstants.PASS_URL_CANCEL, "notUUID"),
                MockMvcRequestBuilders.patch(UrlConstants.PASS_URL_ACTIVATE, "notUUID"),
                MockMvcRequestBuilders.patch(UrlConstants.PASS_URL_UNWARNING, "notUUID"),
                MockMvcRequestBuilders.patch(UrlConstants.PASS_URL_FAVORITE, "notUUID"),
                MockMvcRequestBuilders.patch(UrlConstants.PASS_URL_NOT_FAVORITE, "notUUID"),
                MockMvcRequestBuilders.delete(UrlConstants.PASS_URL + "/{passId}", "notUUD")
        );

    }

}
