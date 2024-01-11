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

import java.util.Optional;

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
