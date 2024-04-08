package ru.ac.checkpointmanager.validation.it;

import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.ac.checkpointmanager.config.OpenAllEndpointsTestConfiguration;
import ru.ac.checkpointmanager.config.ValidationTestConfiguration;
import ru.ac.checkpointmanager.controller.payment.DonationController;
import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;
import ru.ac.checkpointmanager.extension.argprovider.DonationRequestDtoInvalidArgumentsProvider;
import ru.ac.checkpointmanager.service.payment.DonationApiService;
import ru.ac.checkpointmanager.service.payment.DonationService;
import ru.ac.checkpointmanager.util.CheckResultActionsUtils;
import ru.ac.checkpointmanager.util.PaymentMockMvcUtils;

@WebMvcTest(DonationController.class)
@Import({ValidationTestConfiguration.class, OpenAllEndpointsTestConfiguration.class})
@WithMockUser(roles = {"ADMIN"})
@ActiveProfiles("test")
class DonationControllerValidationIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    DonationApiService donationService;

    @ParameterizedTest
    @ArgumentsSource(DonationRequestDtoInvalidArgumentsProvider.class)
    @SneakyThrows
    void donate_BadDonationRequestDto_ReturnValidationError(DonationRequestDto donationRequestDto, String fieldName) {
        ResultActions resultActions = mockMvc.perform(PaymentMockMvcUtils.donate(donationRequestDto))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        CheckResultActionsUtils.checkCommonValidationFields(resultActions);
        CheckResultActionsUtils.checkValidationField(resultActions, 0, fieldName);
    }
}
