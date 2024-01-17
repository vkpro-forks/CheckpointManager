package ru.ac.checkpointmanager.it;

import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.util.ResultCheckUtils;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;

class VisitorNotFoundExceptionHandlerTest extends GlobalExceptionHandlerBasicTestConfig {

    @Test
    @SneakyThrows
    void shouldHandleVisitorNotFoundExceptionForGetVisitor() {
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .get(UrlConstants.VISITOR_URL + "/" + TestUtils.VISITOR_ID));
        ResultCheckUtils.checkNotFoundFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(Matchers.startsWith("Visitor")));
    }

    @Test
    @SneakyThrows
    void shouldHandleVisitorNotFoundExceptionForUpdateVisitor() {
        String visitorDto = TestUtils.jsonStringFromObject(TestUtils.getVisitorDTO());
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders
                .put(UrlConstants.VISITOR_URL + "/" + TestUtils.VISITOR_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(visitorDto));
        ResultCheckUtils.checkNotFoundFields(resultActions);
        resultActions.andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_DETAIL)
                .value(Matchers.startsWith("Visitor")));
    }

}
