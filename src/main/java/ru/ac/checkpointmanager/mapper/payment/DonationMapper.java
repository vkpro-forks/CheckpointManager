package ru.ac.checkpointmanager.mapper.payment;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.payment.DonationRequestDto;
import ru.ac.checkpointmanager.model.payment.Donation;

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
}
