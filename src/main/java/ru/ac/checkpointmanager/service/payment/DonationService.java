package ru.ac.checkpointmanager.service.payment;

import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;
import ru.ac.checkpointmanager.dto.payment.yookassa.PaymentResponse;
import ru.ac.checkpointmanager.model.payment.Donation;

public interface DonationService {

    Donation saveUnconfirmed(DonationRequestDto donationRequestDto);

    Donation confirm(PaymentResponse paymentResponse);
}
