package ru.ac.checkpointmanager.it;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.exception.handler.ErrorCode;
import ru.ac.checkpointmanager.it.config.CorsTestConfiguration;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.util.UrlConstants;
import ru.ac.checkpointmanager.testcontainers.PostgresContainersConfig;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
@Import(CorsTestConfiguration.class)
@ActiveProfiles("test")
class GlobalExceptionHandlerTest extends PostgresContainersConfig {

    @Autowired
    MockMvc mockMvc;

    @Test
    @SneakyThrows
    void handleCarBrandNotFoundException() {
        mockMvc.perform(MockMvcRequestBuilders.get(UrlConstants.CAR_BRANDS_URL+"/"+ UUID.randomUUID()))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath(TestUtils.JSON_ERROR_CODE)
                        .value(ErrorCode.NOT_FOUND.toString()));

    }

    @Test
    void handleConstraintViolationException() {
    }

    @Test
    void handleMethodArgumentNotValidException() {
    }

    @Test
    void handleEntityNotFoundException() {
    }

    @Test
    void handleGeneralException() {
    }

    @Test
    void handleEntranceWasAlreadyException() {
    }

    @Test
    void handleNoActivePassException() {
    }

    @Test
    void handleTerritoryNotFoundException() {
    }

    @Test
    void handlePassNotFoundException() {
    }

    @Test
    void handleIllegalArgumentException() {
    }

    @Test
    void handleAvatarIsTooBigException() {
    }

    @Test
    void handleAvatarNotFoundException() {
    }

    @Test
    void handleAvatarIsEmptyException() {
    }

    @Test
    void handleBadAvatarExtensionException() {
    }

    @Test
    void handleAccessDeniedException() {
    }

    @Test
    void handleUserNotFoundException() {
    }

    @Test
    void handleDateOfBirthFormatException() {
    }

    @Test
    void handleIllegalStateException() {
    }

    @Test
    void handlePhoneAlreadyExistException() {
    }

    @Test
    void handlePhoneNumberNotFoundException() {
    }

    @Test
    void handleUsernameNotFoundException() {
    }

    @Test
    void handleVisitorNotFoundException() {
    }

    @Test
    void handleMailSendException() {
    }

    @Test
    void handleBadCredentialsException() {
    }

    @Test
    void handleInvalidPhoneNumberException() {
    }
}