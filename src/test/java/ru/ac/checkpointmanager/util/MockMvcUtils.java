package ru.ac.checkpointmanager.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.dto.user.UserUpdateDTO;

import java.util.UUID;

@UtilityClass
@Slf4j
public class MockMvcUtils {

    public static MockHttpServletRequestBuilder updatePass(PassUpdateDTO passUpdateDTO) throws JsonProcessingException {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.PUT, UrlConstants.PASS_URL);
        return MockMvcRequestBuilders.put(UrlConstants.PASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.jsonStringFromObject(passUpdateDTO));
    }

    public static MockHttpServletRequestBuilder createPass(PassCreateDTO passCreateDTO) throws JsonProcessingException {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST, UrlConstants.PASS_URL);
        return MockMvcRequestBuilders.post(UrlConstants.PASS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.jsonStringFromObject(passCreateDTO));
    }

    public static MockHttpServletRequestBuilder deletePass(UUID passId) {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.DELETE, UrlConstants.PASS_URL + "/" + passId);
        return MockMvcRequestBuilders.delete(UrlConstants.PASS_URL + "/" + passId);
    }

    public static MockHttpServletRequestBuilder getPass(UUID passId) {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.GET, UrlConstants.PASS_URL + "/" + passId);
        return MockMvcRequestBuilders.get(UrlConstants.PASS_URL + "/" + passId);
    }

    public static MockHttpServletRequestBuilder getPassesByUserId(UUID userId) {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.GET, UrlConstants.PASS_USER_URL, userId);
        return MockMvcRequestBuilders.get(UrlConstants.PASS_USER_URL, userId);
    }

    public static MockHttpServletRequestBuilder getPassesByUsersTerritories(UUID userId) {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.GET, UrlConstants.PASS_USER_TERRITORIES_URL, userId);
        return MockMvcRequestBuilders.get(UrlConstants.PASS_USER_TERRITORIES_URL, userId);
    }

    public static MockHttpServletRequestBuilder getPassesByPartOfVisitorNameAndCarNumber() {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.GET, UrlConstants.PASS_URL_SEARCH);
        return MockMvcRequestBuilders.get(UrlConstants.PASS_URL_SEARCH);
    }

    public static MockHttpServletRequestBuilder updateUser(UserUpdateDTO userUpdateDTO) throws JsonProcessingException {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.PUT, UrlConstants.USER_URL);
        return MockMvcRequestBuilders.put(UrlConstants.USER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.jsonStringFromObject(userUpdateDTO));
    }

}
