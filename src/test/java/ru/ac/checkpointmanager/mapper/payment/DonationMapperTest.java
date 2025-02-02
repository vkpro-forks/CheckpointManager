package ru.ac.checkpointmanager.mapper.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import ru.ac.checkpointmanager.model.payment.Donation;
import ru.ac.checkpointmanager.util.YooKassaTestData;

class DonationMapperTest {

    DonationMapper donationMapper;

    @BeforeEach
    void setUp() {
        donationMapper = new DonationMapper(new ModelMapper());
    }

    @Test
    void paymentResponseToDonation_ExistingDonation_ReturnEnrichDonation() {
        Donation mappedDonation = donationMapper.paymentResponseToDonation(YooKassaTestData.paymentResponse, YooKassaTestData.preSendDonation);

        YooKassaTestData.DONATION_MATCHER.assertMatch(mappedDonation, YooKassaTestData.updatedDonation);
    }

    @Test
    void paymentResponseToDonation_NewDonation_ReturnEnrichDonation() {
        Donation mappedDonation = donationMapper.paymentResponseToDonation(YooKassaTestData.paymentResponse, new Donation());

        YooKassaTestData.DONATION_MATCHER.assertMatch(mappedDonation, YooKassaTestData.newDonationAfterMapping);
    }

    @Test
    void donationRequestDtoToDonation_AllOk_ReturnPreFilledDonation() {
        Donation donation = donationMapper.toDonation(YooKassaTestData.donationRequestDto);

        YooKassaTestData.DONATION_MATCHER.assertMatch(donation, YooKassaTestData.preFilledDonation);
    }
}
