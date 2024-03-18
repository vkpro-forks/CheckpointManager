package ru.ac.checkpointmanager.service.payment;

import ru.ac.checkpointmanager.dto.payment.DonationPerformingResponseDto;
import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;

public interface DonationService {

    DonationPerformingResponseDto makeDonation(DonationRequestDto donationRequestDto);
}
