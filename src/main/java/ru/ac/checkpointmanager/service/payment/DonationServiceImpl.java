package ru.ac.checkpointmanager.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;
import ru.ac.checkpointmanager.dto.payment.yookassa.PaymentResponse;
import ru.ac.checkpointmanager.mapper.payment.DonationMapper;
import ru.ac.checkpointmanager.model.payment.Donation;
import ru.ac.checkpointmanager.repository.payment.DonationRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DonationServiceImpl implements DonationService {

    private final DonationRepository repository;
    private final DonationMapper mapper;

    @Transactional
    @Override
    @NonNull
    public Donation saveUnconfirmed(@NonNull DonationRequestDto donationRequestDto) {
        Donation savedDonation = repository.save(mapper.toDonation(donationRequestDto));
        log.info("New donation {} payment saved to repository", savedDonation.getId());
        return savedDonation;
    }

    @Transactional
    @Override
    @NonNull
    public Donation updateWithPaymentData(@NonNull PaymentResponse paymentResponse) {
        Donation donation = repository.findById(UUID.fromString(paymentResponse.getMetadata().getOrderId()))
                .orElse(new Donation());
        Donation savedDonation = repository.save(mapper.paymentResponseToDonation(paymentResponse, donation));
        log.info("Donation: {} successfully updated with payment data from YooKassa", savedDonation.getId());
        return savedDonation;
    }
}
