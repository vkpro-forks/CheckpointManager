package ru.ac.checkpointmanager.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;
import ru.ac.checkpointmanager.dto.payment.yookassa.PaymentResponse;
import ru.ac.checkpointmanager.mapper.payment.DonationMapper;
import ru.ac.checkpointmanager.model.payment.Donation;
import ru.ac.checkpointmanager.repository.payment.DonationRepository;

import java.util.Optional;
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
    public Donation saveUnconfirmed(DonationRequestDto donationRequestDto) {
        return repository.save(mapper.toDonation(donationRequestDto));
    }

    @Transactional
    @Override
    public Donation confirm(PaymentResponse paymentResponse) {
        Optional<Donation> byId = repository.findById(UUID.fromString(paymentResponse.getMetadata().getOrderId()))
                .orElse()
        return null;
    }
}
