package ru.ac.checkpointmanager.service.payment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ac.checkpointmanager.configuration.payment.YooKassaClient;
import ru.ac.checkpointmanager.dto.payment.DonationPerformingResponseDto;
import ru.ac.checkpointmanager.dto.payment.PaymentRequestDto;
import ru.ac.checkpointmanager.util.YooKassaTestData;

@ExtendWith(MockitoExtension.class)
class DonationYooKassaServiceImplTest {

    @Mock
    DonationService donationService;

    @Mock
    YooKassaClient yooKassaClient;

    @InjectMocks
    DonationYooKassaServiceImpl donationYooKassaService;

    @Captor
    ArgumentCaptor<PaymentRequestDto> requestDtoArgumentCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(donationYooKassaService, "returnUrl", YooKassaTestData.RETURN_URL, String.class);
    }

    @Test
    void makeDonation_StandardFlow_ReturnResponse() {
        Mockito.when(donationService.saveUnconfirmed(Mockito.any())).thenReturn(YooKassaTestData.preSendDonation);
        Mockito.when(yooKassaClient.doPayment(Mockito.any())).thenReturn(YooKassaTestData.PAYMENT_RESPONSE);
        Mockito.when(donationService.updateWithPaymentData(Mockito.any())).thenReturn(YooKassaTestData.updatedDonation);

        DonationPerformingResponseDto donationPerforming = donationYooKassaService
                .makeDonation(YooKassaTestData.donationRequestDto);

        Mockito.verify(donationService, Mockito.times(1)).saveUnconfirmed(Mockito.any());
        Mockito.verify(donationService, Mockito.times(1)).updateWithPaymentData(Mockito.any());
        Mockito.verify(yooKassaClient, Mockito.times(1)).doPayment(requestDtoArgumentCaptor.capture());

        Assertions.assertThat(requestDtoArgumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(YooKassaTestData.paymentRequest);
        Assertions.assertThat(donationPerforming).isEqualTo(YooKassaTestData.donationPerforming);
    }
}