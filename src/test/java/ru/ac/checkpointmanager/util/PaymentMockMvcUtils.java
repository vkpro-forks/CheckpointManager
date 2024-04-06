package ru.ac.checkpointmanager.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.ac.checkpointmanager.controller.payment.DonationController;
import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;

@UtilityClass
@Slf4j
public class PaymentMockMvcUtils {

    public static MockHttpServletRequestBuilder donate(DonationRequestDto donationRequestDto) throws JsonProcessingException {
        log.info(TestMessage.PERFORM_HTTP, HttpMethod.POST.name(), DonationController.REST_URL);
        return MockMvcRequestBuilders.post(DonationController.REST_URL).contentType(MediaType.APPLICATION_JSON)
                .content(TestUtils.jsonStringFromObject(donationRequestDto));
    }
}
