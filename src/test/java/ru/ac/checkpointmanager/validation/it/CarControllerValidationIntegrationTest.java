package ru.ac.checkpointmanager.validation.it;

import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
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
import ru.ac.checkpointmanager.config.CorsTestConfiguration;
import ru.ac.checkpointmanager.config.OpenAllEndpointsTestConfiguration;
import ru.ac.checkpointmanager.config.ValidationTestConfiguration;
import ru.ac.checkpointmanager.controller.car.CarController;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.mapper.CarMapper;
import ru.ac.checkpointmanager.model.avatar.AvatarProperties;
import ru.ac.checkpointmanager.service.car.CarService;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.UUID;
import java.util.stream.Stream;

@WebMvcTest(CarController.class)
@Import({OpenAllEndpointsTestConfiguration.class, CorsTestConfiguration.class,
        AvatarProperties.class, ValidationTestConfiguration.class})
@WithMockUser(roles = {"ADMIN"})
@ActiveProfiles("test")
class CarControllerValidationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CarMapper carMapper;

    @MockBean
    CarService carService;

    @ParameterizedTest
    @MethodSource("getBadCarDto")
    @SneakyThrows
    void shouldReturnValidationErrorForAddCar(CarDTO carDTO) {
        String carDtoString = TestUtils.jsonStringFromObject(carDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CAR_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(carDtoString));
        TestUtils.checkCommonValidationFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers
                .jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0)).value(Matchers.anyOf(
                        Matchers.startsWithIgnoringCase("licensePlate"),
                        Matchers.startsWithIgnoringCase("carBrand"),
                        Matchers.startsWithIgnoringCase("phone")
                )));
    }

    @ParameterizedTest
    @EmptySource
    @SneakyThrows
    void shouldReturnValidationErrorForSearchByPhone(String phone) {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.CAR_PHONE_URL)
                .param("phone", phone));
        TestUtils.checkCommonValidationFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0))
                .value("phone"));
    }


    private static Stream<CarDTO> getBadCarDto() {
        CarDTO badLicensePLate = new CarDTO(UUID.randomUUID(), "NOT A LICENSE PLATE",
                TestUtils.getCarBrandDTO(), TestUtils.PHONE_NUM);
        CarDTO nullLicensePlate = new CarDTO(UUID.randomUUID(), null, TestUtils.getCarBrandDTO(), TestUtils.PHONE_NUM);
        return Stream.of(badLicensePLate, nullLicensePlate);
    }

}
