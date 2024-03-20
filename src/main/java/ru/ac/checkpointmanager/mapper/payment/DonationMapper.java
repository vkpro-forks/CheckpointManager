package ru.ac.checkpointmanager.mapper.payment;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;
import ru.ac.checkpointmanager.dto.payment.yookassa.PaymentResponse;
import ru.ac.checkpointmanager.model.payment.CurrencyEnum;
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
    }

    public Donation toDonation(DonationRequestDto donationRequestDto) {
        return modelMapper.map(donationRequestDto, Donation.class);
    }

    private void configureModelMapper() {

        PropertyMap<PaymentResponse, Donation> donationFromPaymentMap = new PropertyMap<>() {
            @Override
            protected void configure() {
                map().setId(UUID.fromString(source.getMetadata().getOrderId()));
                map().setPaymentId(source.getId());
                map().setAmount(source.getAmount().getValue());
                map().setCurrency(CurrencyEnum.valueOf(source.getAmount().getCurrency()));
                map().setConfirmed(source.getPaid());
                map().setComment(source.getDescription());
                map().setPerformedAt(ZonedDateTime.parse(source.getCreatedAt()));
                map().setStatus(source.getStatus());
                map().setDescription(source.getDescription());
            }
        };
        modelMapper.addMappings(donationFromPaymentMap);

    }
}
