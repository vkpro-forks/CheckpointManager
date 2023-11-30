package ru.ac.checkpointmanager.mapper;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.model.Phone;

import java.util.Collection;
import java.util.List;

@Component
@Slf4j
public class PhoneMapper {
    private final ModelMapper modelMapper;

    public PhoneMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }
    public Phone toPhone(PhoneDTO phoneDTO) {
        return modelMapper.map(phoneDTO, Phone.class);
    }

    public PhoneDTO toPhoneDTO(Phone phone) {
        return modelMapper.map(phone, PhoneDTO.class);
    }

    public List<PhoneDTO> toPhonesDTO(Collection<Phone> phones) {
        return phones.stream()
                .map(p -> modelMapper.map(p, PhoneDTO.class))
                .toList();
    }
}
