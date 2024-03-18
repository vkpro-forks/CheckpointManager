package ru.ac.checkpointmanager.controller.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.dto.payment.DonationPerformingResponseDto;
import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;
import ru.ac.checkpointmanager.service.payment.DonationService;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final DonationService donationService;

    public DonationPerformingResponseDto doPayment(@RequestBody DonationRequestDto donationRequestDto) {
        return donationService.makeDonation(donationRequestDto);
    }

}
