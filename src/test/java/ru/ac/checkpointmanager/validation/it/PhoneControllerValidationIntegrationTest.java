package ru.ac.checkpointmanager.validation.it;

import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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
import ru.ac.checkpointmanager.config.OpenAllEndpointsTestConfiguration;
import ru.ac.checkpointmanager.config.ValidationTestConfiguration;
import ru.ac.checkpointmanager.controller.PhoneController;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.service.phone.PhoneService;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.stream.Stream;

@WebMvcTest(PhoneController.class)
@Import({ValidationTestConfiguration.class, OpenAllEndpointsTestConfiguration.class})
@WithMockUser(roles = {"ADMIN"})
@ActiveProfiles("test")
public class PhoneControllerValidationIntegrationTest {

    private static final String TYPE = "type";

    private static final String USER_ID = "userId";

    private static final String NUMBER = "number";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PhoneService phoneService;

    @ParameterizedTest
    @MethodSource("getBadPhoneDTO")
    @SneakyThrows
    void createPhone_WrongFormat_HandleAndReturnValidationError(PhoneDTO phoneDTO, String field) {
        String phoneDTOToPass = TestUtils.jsonStringFromObject(phoneDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PHONE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(phoneDTOToPass));

        TestUtils.checkCommonValidationFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0))
                .value(field));
    }

    private static Stream<Arguments> getBadPhoneDTO() {
        PhoneDTO noType = TestUtils.getPhoneDto();
        noType.setType(null);
        PhoneDTO noUserId = TestUtils.getPhoneDto();
        noUserId.setUserId(null);
        PhoneDTO emptyNumber = TestUtils.getPhoneDto();
        emptyNumber.setNumber("");
        PhoneDTO nullNumber = TestUtils.getPhoneDto();
        nullNumber.setNumber(null);
        PhoneDTO wrongFormat = TestUtils.getPhoneDto();
        wrongFormat.setNumber("asdf");
        return Stream.of(
                Arguments.of(noType, TYPE),
                Arguments.of(noUserId, USER_ID),
                Arguments.of(emptyNumber, NUMBER),
                Arguments.of(nullNumber, NUMBER),
                Arguments.of(wrongFormat, NUMBER)
        );
    }

}
