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
        Donation mappedDonation = donationMapper.paymentResponseToDonation(YooKassaTestData.PAYMENT_RESPONSE, YooKassaTestData.preDonation);
        YooKassaTestData.DONATION_MATHER.assertMatch(mappedDonation, YooKassaTestData.updatedDonation);
    }

    @Test
    void paymentResponseToDonation_NewDonation_ReturnEnrichDonation() {
        Donation mappedDonation = donationMapper.paymentResponseToDonation(YooKassaTestData.PAYMENT_RESPONSE, new Donation());
        YooKassaTestData.DONATION_MATHER.assertMatch(mappedDonation, YooKassaTestData.newDonationAfterMapping);
    }
}