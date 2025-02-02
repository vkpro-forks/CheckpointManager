package ru.ac.checkpointmanager.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.ac.checkpointmanager.dto.CarBrandDTO;
import ru.ac.checkpointmanager.dto.VisitorDTO;
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

    public static MockHttpServletRequestBuilder getPassesByTerritoryId(UUID territoryId) {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.GET, UrlConstants.PASS_URL_TERRITORY, territoryId);
        return MockMvcRequestBuilders.get(UrlConstants.PASS_URL_TERRITORY, territoryId);
    }

    public static MockHttpServletRequestBuilder getPasses() {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.GET, UrlConstants.PASS_URL);
        return MockMvcRequestBuilders.get(UrlConstants.PASS_URL);
    }

    public static MockHttpServletRequestBuilder updateUser(UserUpdateDTO userUpdateDTO) throws JsonProcessingException {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.PUT, UrlConstants.USER_URL);
        return MockMvcRequestBuilders.put(UrlConstants.USER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.jsonStringFromObject(userUpdateDTO));
    }

    public static MockHttpServletRequestBuilder deleteUser(UUID userId) {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.DELETE, UrlConstants.USER_URL + "/{userId}", userId);
        return MockMvcRequestBuilders.delete(UrlConstants.USER_URL + "/{userId}", userId);
    }

    public static MockHttpServletRequestBuilder uploadAvatarForUser(UUID userId, MockMultipartFile file) {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST, UrlConstants.AVATAR_USER_URL, userId);
        return MockMvcRequestBuilders.multipart(
                HttpMethod.POST, UrlConstants.AVATAR_USER_URL, userId).file(file);
    }

    public static MockHttpServletRequestBuilder uploadAvatarForTerritory(UUID terrId, MockMultipartFile file) {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST, UrlConstants.AVATAR_TERRITORY_URL, terrId);
        return MockMvcRequestBuilders.multipart(
                HttpMethod.POST, UrlConstants.AVATAR_TERRITORY_URL, terrId).file(file);
    }

    public static MockHttpServletRequestBuilder saveCarBrand(CarBrandDTO carBrandDTO) throws JsonProcessingException {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST.name(), UrlConstants.CAR_BRANDS_URL);
        return MockMvcRequestBuilders.post(UrlConstants.CAR_BRANDS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.jsonStringFromObject(carBrandDTO));
    }

    public static MockHttpServletRequestBuilder updateCarBrand(Long carBrandId, CarBrandDTO carBrandDTO) throws JsonProcessingException {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.PUT.name(), UrlConstants.CAR_BRANDS_URL_VAR, carBrandDTO);
        return MockMvcRequestBuilders.put(UrlConstants.CAR_BRANDS_URL_VAR, carBrandId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.jsonStringFromObject(carBrandDTO));
    }

    public static MockHttpServletRequestBuilder createVisitor(VisitorDTO visitorDTO) throws JsonProcessingException {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST.name(), UrlConstants.VISITOR_URL);
        return MockMvcRequestBuilders.post(UrlConstants.VISITOR_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.jsonStringFromObject(visitorDTO));
    }

    public static MockHttpServletRequestBuilder getVisitor(UUID visitorId) {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.GET.name(), UrlConstants.VISITOR_URL_ID, visitorId);
        return MockMvcRequestBuilders.get(UrlConstants.VISITOR_URL_ID, visitorId);
    }

    public static MockHttpServletRequestBuilder updateVisitor(VisitorDTO visitorDTO, UUID visitorId) throws JsonProcessingException {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.PUT.name(), UrlConstants.VISITOR_URL_ID, visitorId);
        return MockMvcRequestBuilders.put(UrlConstants.VISITOR_URL_ID, visitorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.jsonStringFromObject(visitorDTO));
    }

    public static MockHttpServletRequestBuilder deleteVisitor(UUID visitorId) {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.DELETE.name(), UrlConstants.VISITOR_URL_ID, visitorId);
        return MockMvcRequestBuilders.delete(UrlConstants.VISITOR_URL_ID, visitorId);
    }

    public static MockHttpServletRequestBuilder getVisitorByPhonePart() {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.GET.name(), UrlConstants.VISITOR_PHONE_URL);
        return MockMvcRequestBuilders.get(UrlConstants.VISITOR_PHONE_URL);
    }

    public static MockHttpServletRequestBuilder getVisitorByNamePart() {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.GET.name(), UrlConstants.VISITOR_NAME_URL);
        return MockMvcRequestBuilders.get(UrlConstants.VISITOR_NAME_URL);
    }

    public static MockHttpServletRequestBuilder getVisitorByPassId(UUID passId) {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.GET.name(), UrlConstants.VISITOR_PASS_URL, passId);
        return MockMvcRequestBuilders.get(UrlConstants.VISITOR_PASS_URL, passId);
    }

    public static MockHttpServletRequestBuilder getVisitorByUserId(UUID userId) {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.GET.name(), UrlConstants.VISITOR_USER_URL, userId);
        return MockMvcRequestBuilders.get(UrlConstants.VISITOR_USER_URL, userId);
    }

    public static MockHttpServletRequestBuilder searchCarByUserId(UUID userId) {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.GET.name(), UrlConstants.CAR_USER_URL, userId);
        return MockMvcRequestBuilders.get(UrlConstants.CAR_USER_URL, userId);
    }
}
