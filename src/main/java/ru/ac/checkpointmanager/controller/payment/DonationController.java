package ru.ac.checkpointmanager.controller.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.dto.payment.DonationPerformingResponseDto;
import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;
import ru.ac.checkpointmanager.service.payment.DonationYooKassaService;

@RestController
@RequestMapping("/api/v1/donations")
@RequiredArgsConstructor
@Validated
public class DonationController {

    private final DonationYooKassaService donationYooKassaService;

    public DonationPerformingResponseDto donate(@RequestBody DonationRequestDto donationRequestDto) {
        return donationYooKassaService.makeDonation(donationRequestDto);
    }
}
