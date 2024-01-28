package ru.ac.checkpointmanager.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.ac.checkpointmanager.dto.user.UserUpdateDTO;

@UtilityClass
@Slf4j
public class MockMvcUtils {

    public static MockHttpServletRequestBuilder updatePass(String jsonPassUpdateDto) {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.PUT, UrlConstants.PASS_URL);
        return MockMvcRequestBuilders.put(UrlConstants.PASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPassUpdateDto);
    }

    public static MockHttpServletRequestBuilder createPass(String jsonPassCreateDto) {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST, UrlConstants.PASS_URL);
        return MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPassCreateDto);
    }

    public static MockHttpServletRequestBuilder updateUser(UserUpdateDTO userUpdateDTO) throws JsonProcessingException {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.PUT, UrlConstants.USER_URL);
        return MockMvcRequestBuilders.put(UrlConstants.USER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content( TestUtils.jsonStringFromObject(userUpdateDTO));
    }

}
