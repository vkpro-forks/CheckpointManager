package ru.ac.checkpointmanager.mapper.payment;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Condition;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;
import ru.ac.checkpointmanager.dto.payment.yookassa.PaymentResponse;
import ru.ac.checkpointmanager.model.payment.Donation;

import java.time.ZonedDateTime;
import java.util.UUID;

@Component
@Slf4j
public class DonationMapper {

    private final ModelMapper modelMapper;

    @Autowired
    public DonationMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        configureModelMapper();
    }

    public Donation toDonation(DonationRequestDto donationRequestDto) {
        Donation donation = modelMapper.map(donationRequestDto, Donation.class);
        donation.setStatus(Donation.CREATED);
        donation.setConfirmed(false);
        return donation;
    }

    public Donation paymentResponseToDonation(PaymentResponse paymentResponse, Donation donation) {
        modelMapper.map(paymentResponse, donation);
        return donation;
    }

    private void configureModelMapper() {
        configureTypeMapPaymentResponseToDonation();
        configureTypeMapDonationRequestToDonation();
    }

    private void configureTypeMapPaymentResponseToDonation() {
        Condition<PaymentResponse, Donation> isNew = context -> {
            Donation donation = (Donation) context.getParent().getDestination();
            return donation.getId() == null;
        };
        TypeMap<PaymentResponse, Donation> paymentResponseToDonationMap = modelMapper.createTypeMap(PaymentResponse.class, Donation.class);
        paymentResponseToDonationMap.addMappings(mapping -> {
            mapping.when(isNew)
                    .using(ctx -> ctx.getSource() == null ? null : UUID.fromString((String) ctx.getSource()))
                    .map(source -> source.getMetadata().getOrderId(), Donation::setId);
            mapping.when(isNew).map(source -> source.getAmount().getValue(), Donation::setAmount);
            mapping.when(isNew).map(source -> source.getAmount().getCurrency(), Donation::setCurrency);
            mapping.when(isNew).map(PaymentResponse::getDescription, Donation::setComment);
            mapping.map(PaymentResponse::getId, Donation::setPaymentId);
            mapping.map(PaymentResponse::getDescription, Donation::setDescription);
            mapping.map(PaymentResponse::getPaid, Donation::setConfirmed);
            mapping.using(ctx -> ctx.getSource() == null ? null : ZonedDateTime.parse((String) ctx.getSource()))
                    .map(PaymentResponse::getCreatedAt, Donation::setPerformedAt);
            mapping.map(PaymentResponse::getStatus, Donation::setStatus);
        });
    }

    private void configureTypeMapDonationRequestToDonation() {
        TypeMap<DonationRequestDto, Donation> dtoToDonationMap = modelMapper
                .createTypeMap(DonationRequestDto.class, Donation.class);
        dtoToDonationMap.addMappings(mapping -> {
            mapping.map(DonationRequestDto::getAmount, Donation::setAmount);
            mapping.map(DonationRequestDto::getCurrency, Donation::setCurrency);
            mapping.map(DonationRequestDto::getComment, Donation::setComment);
        });
    }
}
