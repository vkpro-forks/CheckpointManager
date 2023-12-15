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
import ru.ac.checkpointmanager.controller.VisitorController;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.mapper.VisitorMapper;
import ru.ac.checkpointmanager.service.visitor.VisitorService;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

import java.util.stream.Stream;

@WebMvcTest(VisitorController.class)
@Import({OpenAllEndpointsTestConfiguration.class, CorsTestConfiguration.class, ValidationTestConfiguration.class})
@WithMockUser(roles = {"ADMIN"})
@ActiveProfiles("test")
class VisitorControllerValidationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    VisitorService visitorService;

    @MockBean
    VisitorMapper visitorMapper;

    @ParameterizedTest
    @SneakyThrows
    @MethodSource("getBadVisitorDto")
    void shouldHandleValidationErrorForBadVisitorDtoForAddVisitor(VisitorDTO visitorDTO) {
        String visitorDtoString = TestUtils.jsonStringFromObject(visitorDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.VISITOR_URL)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(visitorDtoString));
        TestUtils.checkCommonValidationFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0))
                .value(Matchers.anyOf(
                        Matchers.startsWithIgnoringCase("name"),
                        Matchers.startsWithIgnoringCase("phone")
                )));
    }

    @ParameterizedTest
    @SneakyThrows
    @MethodSource("getBadVisitorDto")
    void shouldHandleValidationErrorForBadVisitorDtoForUpdateVisitor(VisitorDTO visitorDTO) {
        String visitorDtoString = TestUtils.jsonStringFromObject(visitorDTO);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .put(UrlConstants.VISITOR_URL + "/" + TestUtils.VISITOR_ID)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(visitorDtoString));
        TestUtils.checkCommonValidationFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0))
                .value(Matchers.anyOf(
                        Matchers.startsWithIgnoringCase("name"),
                        Matchers.startsWithIgnoringCase("phone")
                )));
    }

    @ParameterizedTest
    @EmptySource
    @SneakyThrows
    void shouldHandleValidationErrorForBadPhoneGetByPhone(String phone) {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.VISITOR_PHONE_URL)
                .param("phone", phone));
        TestUtils.checkCommonValidationFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0))
                .value(Matchers.startsWithIgnoringCase("phone")));
    }

    private static Stream<VisitorDTO> getBadVisitorDto() {
        VisitorDTO nullName = new VisitorDTO(TestUtils.VISITOR_ID, null, null, "n");
        VisitorDTO emptyName = new VisitorDTO(TestUtils.VISITOR_ID, "", TestUtils.PHONE_NUM, "n");
        VisitorDTO badPhone = new VisitorDTO(TestUtils.VISITOR_ID, "name", "sdf", "note");
        return Stream.of(
                nullName, emptyName, badPhone
        );
    }


}
