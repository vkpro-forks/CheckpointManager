package ru.ac.checkpointmanager.service.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ac.checkpointmanager.mapper.payment.DonationMapper;
import ru.ac.checkpointmanager.model.payment.Donation;
import ru.ac.checkpointmanager.repository.payment.DonationRepository;
import ru.ac.checkpointmanager.util.YooKassaTestData;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DonationServiceImplTest {

    @Mock
    DonationRepository donationRepository;

    @InjectMocks
    DonationServiceImpl donationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(donationService, "mapper", new DonationMapper(new ModelMapper()), DonationMapper.class);
    }

    @Captor
    ArgumentCaptor<Donation> donationArgumentCaptor;

    @Test
    void saveUnconfirmed_AllOk_returnSavedDonation() {
        Mockito.when(donationRepository.save(Mockito.any())).thenReturn(YooKassaTestData.preSendDonation);

        donationService.saveUnconfirmed(YooKassaTestData.donationRequestDto);

        Mockito.verify(donationRepository).save(donationArgumentCaptor.capture());
        YooKassaTestData.DONATION_MATCHER.assertMatch(donationArgumentCaptor.getValue(), YooKassaTestData.preFilledDonation);
    }

    @Test
    void updateWithPaymentData_WithExistedDonation_ReturnUpdated() {
        Mockito.when(donationRepository.findById(Mockito.any())).thenReturn(Optional.of(YooKassaTestData.preSendDonation));
        Mockito.when(donationRepository.save(Mockito.any())).thenReturn(YooKassaTestData.updatedDonation);

        donationService.updateWithPaymentData(YooKassaTestData.PAYMENT_RESPONSE);

        Mockito.verify(donationRepository, Mockito.times(1)).save(donationArgumentCaptor.capture());
        YooKassaTestData.DONATION_MATCHER.assertMatch(donationArgumentCaptor.getValue(), YooKassaTestData.updatedDonation);
    }

    @Test
    void updateWithPaymentData_DonationAccidentallyNotInRepo_ReturnUpdated() {
        Mockito.when(donationRepository.findById(Mockito.any())).thenReturn(Optional.empty());
        Mockito.when(donationRepository.save(Mockito.any())).thenReturn(YooKassaTestData.newDonationAfterMapping);

        donationService.updateWithPaymentData(YooKassaTestData.PAYMENT_RESPONSE);

        Mockito.verify(donationRepository, Mockito.times(1)).save(donationArgumentCaptor.capture());
        YooKassaTestData.DONATION_MATCHER.assertMatch(donationArgumentCaptor.getValue(), YooKassaTestData.newDonationAfterMapping);
    }
}
