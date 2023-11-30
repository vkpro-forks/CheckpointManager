package ru.ac.checkpointmanager.validation;

import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.configuration.JwtAuthenticationFilter;
import ru.ac.checkpointmanager.configuration.JwtService;
import ru.ac.checkpointmanager.controller.PassController;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.dto.passes.PassDtoCreate;
import ru.ac.checkpointmanager.dto.passes.PassDtoUpdate;
import ru.ac.checkpointmanager.exception.handler.ErrorCode;
import ru.ac.checkpointmanager.it.config.CorsTestConfiguration;
import ru.ac.checkpointmanager.it.config.OpenAllEndpointsTestConfiguration;
import ru.ac.checkpointmanager.mapper.PassMapper;
import ru.ac.checkpointmanager.repository.TokenRepository;
import ru.ac.checkpointmanager.service.passes.PassService;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

@WebMvcTest(PassController.class)
@Import({OpenAllEndpointsTestConfiguration.class, CorsTestConfiguration.class})
@WithMockUser(roles = {"ADMIN"})
public class PassControllerValidationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PassService passService;

    @MockBean
    PassMapper passMapper;

    @Autowired
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    JwtService jwtService;

    @MockBean
    TokenRepository tokenRepository;

    @MockBean
    UserDetailsService userDetailsService;

    @Test
    @SneakyThrows
    void shouldReturnValidationErrorForNullCarAndVisitorFieldsForAddPass() {
        PassDtoCreate passDtoCreate = TestUtils.getPassDtoCreate();
        passDtoCreate.setCar(null);
        passDtoCreate.setVisitor(null);
        String passDtoCreateString = TestUtils.jsonStringFromObject(passDtoCreate);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passDtoCreateString));
        checkFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldReturnValidationErrorForBothCarAndVisitorFieldsForAddPass() {
        PassDtoCreate passDtoCreate = TestUtils.getPassDtoCreate();
        passDtoCreate.setCar(new CarDTO());
        passDtoCreate.setVisitor(new VisitorDTO());
        String passDtoCreateString = TestUtils.jsonStringFromObject(passDtoCreate);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passDtoCreateString));
        checkFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldReturnValidationErrorForNullCarAndVisitorFieldsForUpdatePass() {
        PassDtoUpdate passDtoUpdate = TestUtils.getPassDtoUpdate();
        passDtoUpdate.setCar(null);
        passDtoUpdate.setVisitor(null);
        String passDtoCreateString = TestUtils.jsonStringFromObject(passDtoUpdate);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.PASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passDtoCreateString));
        checkFields(resultActions);
    }

    @Test
    @SneakyThrows
    void shouldReturnValidationErrorForBothCarAndVisitorFieldsForUpdatePass() {
        PassDtoUpdate passDtoUpdate = TestUtils.getPassDtoUpdate();
        passDtoUpdate.setCar(new CarDTO());
        passDtoUpdate.setVisitor(new VisitorDTO());
        String passDtoCreateString = TestUtils.jsonStringFromObject(passDtoUpdate);
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.put(UrlConstants.PASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(passDtoCreateString));
        checkFields(resultActions);
    }


    private static void checkFields(ResultActions resultActions) throws Exception {
        resultActions
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.VALIDATION.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(0))
                        .value(Matchers.anyOf(Matchers.is("car"), Matchers.is("visitor"))))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_VIOLATIONS_FIELD.formatted(1))
                        .value(Matchers.anyOf(Matchers.is("car"), Matchers.is("visitor"))));
    }

}
