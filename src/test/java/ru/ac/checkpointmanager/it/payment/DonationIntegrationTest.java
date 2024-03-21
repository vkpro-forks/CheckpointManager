package ru.ac.checkpointmanager.it.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Body;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.assertion.DonationPerformingResultActionsAssert;
import ru.ac.checkpointmanager.assertion.ErrorResponseResultActionsAssert;
import ru.ac.checkpointmanager.config.EnablePostgresAndRedisTestContainers;
import ru.ac.checkpointmanager.config.annotation.MockMvcIntegrationTest;
import ru.ac.checkpointmanager.config.payment.HttpClientTestConfiguration;
import ru.ac.checkpointmanager.dto.payment.yookassa.ErrorYooKassaResponse;
import ru.ac.checkpointmanager.exception.handler.ErrorCode;
import ru.ac.checkpointmanager.exception.handler.ErrorMessage;
import ru.ac.checkpointmanager.exception.payment.DonationException;
import ru.ac.checkpointmanager.model.payment.CurrencyEnum;
import ru.ac.checkpointmanager.util.PaymentMockMvcUtils;
import ru.ac.checkpointmanager.util.YooKassaTestData;

import java.math.BigDecimal;

@MockMvcIntegrationTest
@WithMockUser(roles = {"ADMIN"})
@EnablePostgresAndRedisTestContainers
@EnableWireMock({
        @ConfigureWireMock(name = "yookassa-api", property = "yookassa.url")})
@Import(HttpClientTestConfiguration.class)
class DonationIntegrationTest {

    public static final String CONTENT_TYPE = "Content-Type";
    @InjectWireMock("yookassa-api")
    private WireMockServer wireMockServer;

    @Value("${yookassa.url}")
    private String wiremockUrl;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @SneakyThrows
    void donate_AllOk_ReturnDtoWithPaymentUrl() {
        wireMockServer.stubFor(WireMock.post("/payments").willReturn(WireMock.ok()
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withResponseBody(Body.fromJsonBytes(objectMapper.writeValueAsBytes(YooKassaTestData.PAYMENT_RESPONSE)))));

        ResultActions resultActions = mockMvc.perform(PaymentMockMvcUtils.donate(YooKassaTestData.donationRequestDto))
                .andExpect(MockMvcResultMatchers.status().isOk());
        DonationPerformingResultActionsAssert.assertThat(resultActions).returnUrlMatches(YooKassaTestData.PAYMENT_URL)
                .amountMatches(BigDecimal.TEN, CurrencyEnum.RUB).descriptionMatches(YooKassaTestData.COMMENT);
    }

    @Test
    @SneakyThrows
    void donate_ApiReturnBadRequest_ReturnErrorMessage() {
        ErrorYooKassaResponse yooKassa400Error = YooKassaTestData.yooKassa400Error;
        wireMockServer.stubFor(WireMock.post("/payments").willReturn(WireMock.badRequest()
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withResponseBody(Body.fromJsonBytes(objectMapper.writeValueAsBytes(yooKassa400Error)))));

        ResultActions resultActions = mockMvc.perform(PaymentMockMvcUtils.donate(YooKassaTestData.donationRequestDto))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());

        ErrorResponseResultActionsAssert.assertThat(resultActions).errorCodeMatches(ErrorCode.INTERNAL_SERVER_ERROR)
                .timeStampNotEmpty().detailsIsNotEmpty().contentTypeIsApplicationProblemJson()
                .titleMatches(ErrorMessage.DONATION_ERROR)
                .detailsMatches(DonationException.MESSAGE.formatted(yooKassa400Error.getType() + ": " + yooKassa400Error.getDescription()));
    }

    @Test
    @SneakyThrows
    void donate_ApiReturn500_ReturnErrorMessage() {
        ErrorYooKassaResponse yooKassa400Error = YooKassaTestData.yooKassa400Error;
        wireMockServer.stubFor(WireMock.post("/payments").willReturn(WireMock.serverError()
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withResponseBody(Body.fromJsonBytes(objectMapper.writeValueAsBytes(yooKassa400Error)))));

        ResultActions resultActions = mockMvc.perform(PaymentMockMvcUtils.donate(YooKassaTestData.donationRequestDto))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());

        ErrorResponseResultActionsAssert.assertThat(resultActions).errorCodeMatches(ErrorCode.INTERNAL_SERVER_ERROR)
                .timeStampNotEmpty().detailsIsNotEmpty().contentTypeIsApplicationProblemJson()
                .titleMatches(ErrorMessage.DONATION_ERROR)
                .detailsMatches(DonationException.MESSAGE.formatted("status: Internal Server Error"));
    }
}
