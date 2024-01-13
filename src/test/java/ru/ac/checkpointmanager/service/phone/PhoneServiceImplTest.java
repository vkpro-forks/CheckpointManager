package ru.ac.checkpointmanager.service.phone;

import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.exception.ObjectAlreadyExistsException;
import ru.ac.checkpointmanager.exception.PhoneAlreadyExistException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.PhoneMapper;
import ru.ac.checkpointmanager.model.Phone;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.PhoneRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.util.TestUtils;
import ru.ac.checkpointmanager.utils.FieldsValidation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class PhoneServiceImplTest {

    @Mock
    PhoneRepository phoneRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    PhoneServiceImpl phoneService;

    @Captor
    ArgumentCaptor<Phone> phoneArgumentCaptor;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(phoneService, "phoneMapper", new PhoneMapper(new ModelMapper()));
    }

    @Test
    void findByIdReturnPhoneDTO() {
        Phone phone = TestUtils.getPhone();
        UUID phoneId = phone.getId();

        Mockito.when(phoneRepository.findById(phoneId)).thenReturn(Optional.of(phone));
        PhoneDTO result = phoneService.findById(phoneId);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(phoneId);
        Mockito.verify(phoneRepository).findById(phoneId);
    }

    @Test
    void findByIdNotFoundException() {
        Phone phone = TestUtils.getPhone();
        UUID phoneId = phone.getId();

        Mockito.when(phoneRepository.findById(phoneId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(
                        () -> phoneService.findById(phoneId))
                .isInstanceOf(EntityNotFoundException.class);
        Mockito.verify(phoneRepository).findById(phoneId);
    }

    @Test
    void updatePhoneNumberSuccessful() {
        Phone phone = TestUtils.getPhone();
        UUID phoneId = phone.getId();
        PhoneDTO phoneDTO = TestUtils.getPhoneDto();
        phoneDTO.setId(phoneId);
        phoneDTO.setNumber(FieldsValidation.cleanPhone(phoneDTO.getNumber()));

        Mockito.when(phoneRepository.findById(phoneId)).thenReturn(Optional.of(phone));
        Mockito.when(phoneRepository.existsByNumber(phoneDTO.getNumber())).thenReturn(false);
        PhoneDTO result = phoneService.updatePhoneNumber(phoneDTO);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(phoneId);
        Assertions.assertThat(result.getNumber()).isEqualTo(phoneDTO.getNumber());
        Assertions.assertThat(result.getType()).isEqualTo(phoneDTO.getType());

        Mockito.verify(phoneRepository).findById(phoneId);
        Mockito.verify(phoneRepository).existsByNumber(phoneDTO.getNumber());
        Mockito.verify(phoneRepository).save(Mockito.any(Phone.class));
    }

    @Test
    void updatePhoneNotFoundException() {
        Phone phone = TestUtils.getPhone();
        UUID phoneId = phone.getId();
        PhoneDTO phoneDTO = TestUtils.getPhoneDto();
        phoneDTO.setId(phoneId);

        Mockito.when(phoneRepository.findById(phoneId)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(
                        () -> phoneService.updatePhoneNumber(phoneDTO))
                .isInstanceOf(EntityNotFoundException.class);

        Mockito.verify(phoneRepository).findById(phoneId);
    }

    @Test
    void updatePhoneAlreadyTakenException() {
        Phone phone = TestUtils.getPhone();
        UUID phoneId = phone.getId();
        PhoneDTO phoneDTO = TestUtils.getPhoneDto();
        phoneDTO.setId(phoneId);
        phoneDTO.setNumber(FieldsValidation.cleanPhone(phoneDTO.getNumber()));

        Mockito.when(phoneRepository.findById(phoneId)).thenReturn(Optional.of(phone));
        Mockito.when(phoneRepository.existsByNumber(phoneDTO.getNumber())).thenReturn(true);

        Assertions.assertThatExceptionOfType(PhoneAlreadyExistException.class)
                .isThrownBy(() -> phoneService.updatePhoneNumber(phoneDTO))
                .withMessageContaining("already exist");
    }

    @Test
    void deletePhoneNumberSuccessful() {
        Phone phone = TestUtils.getPhone();
        UUID phoneId = phone.getId();

        Mockito.when(phoneRepository.findById(phoneId)).thenReturn(Optional.of(phone));
        phoneService.deletePhoneNumber(phoneId);
        Mockito.verify(phoneRepository).deleteById(phoneId);
    }

    @Test
    void deletePhoneNumberNotFoundException() {
        Phone phone = TestUtils.getPhone();
        UUID phoneId = phone.getId();

        Mockito.when(phoneRepository.findById(phoneId)).thenReturn(Optional.empty());
        Assertions.assertThatThrownBy(() -> phoneService.deletePhoneNumber(phoneId))
                .isInstanceOf(EntityNotFoundException.class);
        Mockito.verify(phoneRepository, Mockito.never()).deleteById(phoneId);
    }

    @Test
    void getAllReturnPhoneDTOCollection() {
        Phone phone = TestUtils.getPhone();
        List<Phone> phones = List.of(phone);
        Mockito.when(phoneRepository.findAll()).thenReturn(phones);

        Collection<PhoneDTO> result = phoneService.getAll();
        Assertions.assertThat(result.size()).isEqualTo(phones.size());
    }

    @Test
    void getAllReturnEmptyCollectionWithoutException() {
        Mockito.when(phoneRepository.findAll()).thenReturn(Collections.emptyList());
        Assertions.assertThatNoException().isThrownBy(() -> phoneService.getAll());
    }

    @Test
    void createPhoneNumber_AllOk_SavePhoneNumber() {
        User user = TestUtils.getUser();
        PhoneDTO phoneDto = TestUtils.getPhoneDto();
        Mockito.when(phoneRepository.existsByNumber(Mockito.any())).thenReturn(false);
        Mockito.when(userRepository.findById(phoneDto.getUserId())).thenReturn(Optional.of(user));
        Mockito.when(phoneRepository.save(Mockito.any())).thenReturn(new Phone());
        phoneService.createPhoneNumber(phoneDto);

        Mockito.verify(phoneRepository).save(phoneArgumentCaptor.capture());
        Phone captured = phoneArgumentCaptor.getValue();

        Assertions.assertThat(captured.getNumber()).as("Check if phone number the same as in dto")
                .isEqualTo(FieldsValidation.cleanPhone(phoneDto.getNumber()));
        Assertions.assertThat(captured.getUser()).as("Check if user the same as in dto")
                .isEqualTo(user);
        Assertions.assertThat(captured.getType()).as("Check is type the same as in dto")
                .isEqualTo(phoneDto.getType());
    }

    @Test
    void createPhoneNumber_UserNotFound_ThrowException() {
        PhoneDTO phoneDto = TestUtils.getPhoneDto();
        Mockito.when(userRepository.findById(phoneDto.getUserId())).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> phoneService.createPhoneNumber(phoneDto))
                .isInstanceOf(EntityNotFoundException.class);

        Mockito.verifyNoInteractions(phoneRepository);
    }

    @Test
    void createPhoneNumber_PhoneNumberExists_ThrowException() {
        User user = TestUtils.getUser();
        PhoneDTO phoneDto = TestUtils.getPhoneDto();
        Mockito.when(phoneRepository.existsByNumber(Mockito.any())).thenReturn(true);
        Mockito.when(userRepository.findById(phoneDto.getUserId())).thenReturn(Optional.of(user));

        Assertions.assertThatExceptionOfType(PhoneAlreadyExistException.class)
                .isThrownBy(() -> phoneService.createPhoneNumber(phoneDto))
                .isInstanceOf(ObjectAlreadyExistsException.class);

        Mockito.verify(phoneRepository, Mockito.never()).save(Mockito.any());
    }
}

