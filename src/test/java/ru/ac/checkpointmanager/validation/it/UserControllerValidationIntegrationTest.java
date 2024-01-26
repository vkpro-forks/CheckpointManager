package ru.ac.checkpointmanager.validation.it;

import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
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
import ru.ac.checkpointmanager.controller.UserController;
import ru.ac.checkpointmanager.dto.user.UserUpdateDTO;
import ru.ac.checkpointmanager.service.user.UserService;
import ru.ac.checkpointmanager.util.ResultCheckUtils;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.stream.Stream;

@WebMvcTest(UserController.class)
@Import({ValidationTestConfiguration.class, OpenAllEndpointsTestConfiguration.class})
@WithMockUser(roles = {"ADMIN"})
@ActiveProfiles("test")
class UserControllerValidationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @ParameterizedTest
    @MethodSource("getBadPhones")
    @SneakyThrows
    void updateUser_WrongPhone_handleAndReturnValidationError(String phone) {
        UserUpdateDTO userUpdateDTO = TestUtils.getUserUpdateDTO();
        userUpdateDTO.setMainNumber(phone);
        String userPutDTOString = TestUtils.jsonStringFromObject(userUpdateDTO);

        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.USER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(userPutDTOString));
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0))
                .value("mainNumber"));
        ResultCheckUtils.checkCommonValidationFields(resultActions);
    }

    private static Stream<String> getBadPhones() {
        return Stream.of(
                "", "1".repeat(10), "1".repeat(21), "123456789011asdf"
        );
    }

}
