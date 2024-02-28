package ru.ac.checkpointmanager.validation.it;

import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.OpenAllEndpointsTestConfiguration;
import ru.ac.checkpointmanager.config.ValidationTestConfiguration;
import ru.ac.checkpointmanager.controller.car.CarController;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.mapper.CarMapper;
import ru.ac.checkpointmanager.model.avatar.AvatarProperties;
import ru.ac.checkpointmanager.service.car.CarService;
import ru.ac.checkpointmanager.util.CheckResultActionsUtils;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.UUID;
import java.util.stream.Stream;

@WebMvcTest(CarController.class)
@Import({AvatarProperties.class, ValidationTestConfiguration.class, OpenAllEndpointsTestConfiguration.class})
@WithMockUser(roles = {"ADMIN"})
@ActiveProfiles("test")
class CarControllerValidationIntegrationTest {

    private static final String PHONE = "phone";
    private static final String LICENSE_PLATE = "licensePlate";
    private static final String CAR_BRAND = "carBrand";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CarMapper carMapper;

    @MockBean
    CarService carService;

    @ParameterizedTest
    @MethodSource("getBadCarDto")
    @SneakyThrows
    void addCar_BadCarDTO_HandleValidationErrorAndReturnBadRequest(CarDTO carDTO) {
        String carDtoString = TestUtils.jsonStringFromObject(carDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CAR_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(carDtoString));

        CheckResultActionsUtils.checkCommonValidationFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers
                .jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0)).value(Matchers.anyOf(
                        Matchers.startsWithIgnoringCase(LICENSE_PLATE),
                        Matchers.startsWithIgnoringCase(CAR_BRAND),
                        Matchers.startsWithIgnoringCase(PHONE)
                )));
    }

    @ParameterizedTest
    @MethodSource("getBadCarDto")
    @SneakyThrows
    void updateCar_BadCarDTO_HandleValidationErrorAndReturnBadRequest(CarDTO carDTO) {
        String carDtoString = TestUtils.jsonStringFromObject(carDTO);

        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put(UrlConstants.CAR_URL + "/{carId}", TestUtils.CAR_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(carDtoString));

        CheckResultActionsUtils.checkCommonValidationFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers
                .jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0)).value(Matchers.anyOf(
                        Matchers.startsWithIgnoringCase(LICENSE_PLATE),
                        Matchers.startsWithIgnoringCase(CAR_BRAND),
                        Matchers.startsWithIgnoringCase(PHONE)
                )));
    }

    @Test
    @SneakyThrows
    void updateCar_BadUUIDPassed_HandleErrorAndReturnBadRequest() {
        CarDTO carDto = TestUtils.getCarDto();
        String carDtoString = TestUtils.jsonStringFromObject(carDto);

        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.put(UrlConstants.CAR_URL + "/{carId}", "123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(carDtoString));

        CheckResultActionsUtils.checkWrongTypeFields(resultActions);
    }

    @Test
    @SneakyThrows
    void deleteCar_BadUUIDPassed_HandleErrorAndReturnBadRequest() {
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete(UrlConstants.CAR_URL + "/{carId}", "123"));

        CheckResultActionsUtils.checkWrongTypeFields(resultActions);
    }

    @Test
    @SneakyThrows
    void searchByUserId_BadUUIDPassed_HandleErrorAndReturnBadRequest() {
        ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(UrlConstants.CAR_USER_URL, "123"));

        CheckResultActionsUtils.checkWrongTypeFields(resultActions);
    }

    @ParameterizedTest
    @EmptySource
    @SneakyThrows
    void searchByPhone_EmptyPhone_ReturnValidationError(String phone) {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.CAR_PHONE_URL)
                .param(PHONE, phone));

        CheckResultActionsUtils.checkCommonValidationFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0))
                .value(PHONE));
    }

    @ParameterizedTest
    @NullSource
    @SneakyThrows
    void searchByPhone_NullPhoneParam_ReturnValidationError(String phone) {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.CAR_PHONE_URL)
                .param(PHONE, phone));

        CheckResultActionsUtils.checkMissingRequestParamFields(resultActions);
    }

    private static Stream<CarDTO> getBadCarDto() {
        CarDTO badLicensePLate = new CarDTO(UUID.randomUUID(), "NOT A LICENSE PLATE",
                TestUtils.getCarBrandDTO(), TestUtils.PHONE_NUM);
        CarDTO nullLicensePlate = new CarDTO(UUID.randomUUID(), null, TestUtils.getCarBrandDTO(), TestUtils.PHONE_NUM);
        return Stream.of(badLicensePLate, nullLicensePlate);
    }

}
