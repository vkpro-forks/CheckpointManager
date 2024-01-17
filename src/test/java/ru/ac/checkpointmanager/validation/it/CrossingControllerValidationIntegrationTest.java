package ru.ac.checkpointmanager.validation.it;

import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import ru.ac.checkpointmanager.controller.CrossingController;
import ru.ac.checkpointmanager.dto.CrossingRequestDTO;
import ru.ac.checkpointmanager.model.avatar.AvatarProperties;
import ru.ac.checkpointmanager.service.crossing.CrossingService;
import ru.ac.checkpointmanager.util.ResultCheckUtils;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.time.ZonedDateTime;
import java.util.stream.Stream;

@WebMvcTest(CrossingController.class)
@Import({AvatarProperties.class, ValidationTestConfiguration.class, OpenAllEndpointsTestConfiguration.class})
@WithMockUser(roles = {"ADMIN"})
@ActiveProfiles("test")
public class CrossingControllerValidationIntegrationTest {

    public static final String IN = "/in";
    public static final String OUT = "/out";
    @Autowired
    MockMvc mockMvc;

    @MockBean
    CrossingService crossingService;

    @ParameterizedTest
    @MethodSource("getWrongZdt")
    @SneakyThrows
    void shouldReturnValidationErrorWithWrongDatePassed(ZonedDateTime zonedDateTime, String direction) {
        CrossingRequestDTO crossingRequestDTO = new CrossingRequestDTO(TestUtils.PASS_ID, TestUtils.CHECKPOINT_ID,
                zonedDateTime);
        String crossingDtoString = TestUtils.jsonStringFromObject(crossingRequestDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_URL + direction)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crossingDtoString))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0))
                        .value("performedAt"));

        ResultCheckUtils.checkCommonValidationFields(resultActions);
    }

    @ParameterizedTest
    @ValueSource(strings = {IN, OUT})
    @SneakyThrows
    void shouldReturnValidationErrorWithNullPassId(String direction) {
        CrossingRequestDTO crossingRequestDTO = new CrossingRequestDTO(null, TestUtils.CHECKPOINT_ID,
                ZonedDateTime.now());
        String crossingDtoString = TestUtils.jsonStringFromObject(crossingRequestDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_URL + direction)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crossingDtoString))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0))
                        .value("passId"));

        ResultCheckUtils.checkCommonValidationFields(resultActions);
    }

    @ParameterizedTest
    @ValueSource(strings = {IN, OUT})
    @SneakyThrows
    void shouldReturnValidationErrorWithNullCheckpointId(String direction) {
        CrossingRequestDTO crossingRequestDTO = new CrossingRequestDTO(TestUtils.PASS_ID, null,
                ZonedDateTime.now());
        String crossingDtoString = TestUtils.jsonStringFromObject(crossingRequestDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.CROSSING_URL + direction)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crossingDtoString))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0))
                        .value("checkpointId"));

        ResultCheckUtils.checkCommonValidationFields(resultActions);
    }


    private static Stream<Arguments> getWrongZdt() {
        return Stream.of(
                Arguments.of(null, IN),
                Arguments.of(ZonedDateTime.now().plusHours(1), IN),
                Arguments.of(null, OUT),
                Arguments.of(ZonedDateTime.now().plusHours(1), OUT)
        );
    }

}
