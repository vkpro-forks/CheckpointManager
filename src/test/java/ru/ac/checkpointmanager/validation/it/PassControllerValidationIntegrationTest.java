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
import ru.ac.checkpointmanager.util.ResultCheckUtils;
import ru.ac.checkpointmanager.util.TestUtils;

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
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        passCreateDTO.setCar(null);
        passCreateDTO.setVisitor(null);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        checkCarOrVisitorFields(resultActions);
    }

    @Test
    @SneakyThrows
    void addPass_BothCarAndVisitorFieldsForAddPass_HandleExceptionAndReturnValidationError() { // testing CarOrVisitorFieldsCheck
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        passCreateDTO.setVisitor(TestUtils.getVisitorDTO());

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        checkCarOrVisitorFields(resultActions);
    }

    @Test
    @SneakyThrows
    void updatePass_NullCarAndVisitorFields_HandleExceptionAndReturnValidationError() { // testing CarOrVisitorFieldsCheck
        PassUpdateDTO passUpdateDTO = TestUtils.getPassUpdateDTOWithCar();
        passUpdateDTO.setCar(null);
        passUpdateDTO.setVisitor(null);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.updatePass(passUpdateDTO));

        checkCarOrVisitorFields(resultActions);
    }

    @Test
    @SneakyThrows
    void updatePass_BothCarAndVisitorFields_HandleExceptionAndReturnValidationError() { // testing CarOrVisitorFieldsCheck
        PassUpdateDTO passUpdateDTO = TestUtils.getPassUpdateDTOWithCar();
        passUpdateDTO.setVisitor(TestUtils.getVisitorDTO());

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.updatePass(passUpdateDTO));

        checkCarOrVisitorFields(resultActions);
    }

    @Test
    @SneakyThrows
    void addPass_IncorrectStartAndEndTimeFields_HandleExceptionAndReturnValidationError() { // testing PassTimeCheck
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        passCreateDTO.setEndTime(LocalDateTime.now().plusHours(1));
        passCreateDTO.setStartTime(LocalDateTime.now().plusHours(3));

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        checkStartEndTimeFields(resultActions);
    }

    @Test
    @SneakyThrows
    void updatePass_IncorrectStartAndEndTimeFields_HandleExceptionAndReturnValidationError() { // testing PassTimeCheck
        PassUpdateDTO passUpdateDTO = TestUtils.getPassUpdateDTOWithCar();
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
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        passCreateDTO.setEndTime(startTime);
        passCreateDTO.setStartTime(endTime);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        ResultCheckUtils.checkCommonValidationFields(resultActions);
    }

    @ParameterizedTest
    @MethodSource("getGoodPassTimeAndNull")
    @SneakyThrows
    void updatePass_IfStartOrEndTimeOfPassIsNullInPassUpdateDTO_HandleExceptionAndReturnValidationError(
            LocalDateTime startTime, LocalDateTime endTime) { // testing NotNull
        PassUpdateDTO passUpdateDTO = TestUtils.getPassUpdateDTOWithCar();
        passUpdateDTO.setEndTime(startTime);
        passUpdateDTO.setStartTime(endTime);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.updatePass(passUpdateDTO));

        ResultCheckUtils.checkCommonValidationFields(resultActions);
    }

    @Test
    @SneakyThrows
    void addPass_EndTimeNotInFuture_HandleExceptionAndReturnValidationError() { // testing Future
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        passCreateDTO.setEndTime(LocalDateTime.now().minusDays(1));

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        ResultCheckUtils.checkCommonValidationFields(resultActions);
        ResultCheckUtils.checkValidationField(resultActions, 0, END_TIME);
    }

    @Test
    @SneakyThrows
    void updatePass_EndTimeNotInFuture_HandleExceptionAndReturnValidationError() { // testing Future
        PassUpdateDTO passUpdateDTO = TestUtils.getPassUpdateDTOWithCar();
        passUpdateDTO.setEndTime(LocalDateTime.now().minusDays(1));

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.updatePass(passUpdateDTO));

        ResultCheckUtils.checkCommonValidationFields(resultActions);
        ResultCheckUtils.checkValidationField(resultActions, 0, END_TIME);
    }


    @ParameterizedTest
    @MethodSource("getBadVisitorDto")
    @SneakyThrows
    void addPass_VisitorHasBadField_HandleExceptionAndReturnValidationError(String name, String phone, String field) {
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithVisitor();
        passCreateDTO.getVisitor().setName(name);
        passCreateDTO.getVisitor().setPhone(phone);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        ResultCheckUtils.checkCommonValidationFields(resultActions);
        ResultCheckUtils.checkValidationField(resultActions, 0, field);
    }

    @ParameterizedTest
    @MethodSource("getBadVisitorDto")
    @SneakyThrows
    void updatePass_VisitorHasBadField_HandleExceptionAndReturnValidationError(String name, String phone, String field) {
        PassUpdateDTO passUpdateDTO = TestUtils.getPassUpdateDTOVisitor();
        passUpdateDTO.getVisitor().setName(name);
        passUpdateDTO.getVisitor().setPhone(phone);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.updatePass(passUpdateDTO));

        ResultCheckUtils.checkCommonValidationFields(resultActions);
        ResultCheckUtils.checkValidationField(resultActions, 0, field);
    }

    @ParameterizedTest
    @MethodSource("getBadCarDto")
    @SneakyThrows
    void addPass_CarHasBadField_HandleExceptionAndReturnValidationError(String licensePlate, CarBrandDTO carBrandDTO,
                                                                        String phone, String field) {
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        CarDTO car = passCreateDTO.getCar();
        car.setLicensePlate(licensePlate);
        car.setPhone(phone);
        car.setBrand(carBrandDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        ResultCheckUtils.checkCommonValidationFields(resultActions);
        ResultCheckUtils.checkValidationField(resultActions, 0, field);
    }

    @ParameterizedTest
    @MethodSource("getBadCarDto")
    @SneakyThrows
    void updatePass_CarHasBadField_HandleExceptionAndReturnValidationError(String licensePlate, CarBrandDTO carBrandDTO,
                                                                           String phone, String field) {
        PassUpdateDTO passUpdateDTO = TestUtils.getPassUpdateDTOWithCar();
        CarDTO car = passUpdateDTO.getCar();
        car.setLicensePlate(licensePlate);
        car.setPhone(phone);
        car.setBrand(carBrandDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.updatePass(passUpdateDTO));

        ResultCheckUtils.checkCommonValidationFields(resultActions);
        ResultCheckUtils.checkValidationField(resultActions, 0, field);
    }

    @ParameterizedTest
    @MethodSource("getNullIds")
    @SneakyThrows
    void addPass_NullUserIdOrTerritoryId_HandleExceptionAndReturnValidationError(UUID userId, UUID terrId, String field) {
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithVisitor();
        passCreateDTO.setTerritoryId(terrId);
        passCreateDTO.setUserId(userId);

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        ResultCheckUtils.checkCommonValidationFields(resultActions);
        ResultCheckUtils.checkValidationField(resultActions, 0, field);
    }

    @Test
    @SneakyThrows
    void addPass_TooLongComment_HandleExceptionAndReturnValidationError() {
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithVisitor();
        passCreateDTO.setComment("a".repeat(31));

        ResultActions resultActions = mockMvc.perform(MockMvcUtils.createPass(passCreateDTO));

        ResultCheckUtils.checkCommonValidationFields(resultActions);
        ResultCheckUtils.checkValidationField(resultActions, 0, "comment");
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

}
