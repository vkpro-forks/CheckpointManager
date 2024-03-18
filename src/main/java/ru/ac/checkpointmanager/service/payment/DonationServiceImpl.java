package ru.ac.checkpointmanager.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.dto.payment.DonationPerformingResponseDto;
import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonationServiceImpl implements DonationService {
    @Override
    public DonationPerformingResponseDto makeDonation(DonationRequestDto donationRequestDto) {
        //save payment info to DB
        //send request
        //update payment info in DB
        //send response to user
        return null;
    }
}
