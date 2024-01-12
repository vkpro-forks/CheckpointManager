package ru.ac.checkpointmanager.service.phone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ac.checkpointmanager.mapper.PhoneMapper;
import ru.ac.checkpointmanager.repository.PhoneRepository;

@ExtendWith(MockitoExtension.class)
class PhoneServiceImplTest {

    @Mock
    PhoneRepository phoneRepository;

    @InjectMocks
    PhoneServiceImpl phoneService;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(phoneService, "phoneMapper", new PhoneMapper(new ModelMapper()));
    }

    @Test
    void createPhoneNumber() {
    }

    @Test
    void findById() {
    }

    @Test
    void findPhoneById() {
    }

    @Test
    void updatePhoneNumber() {
    }

    @Test
    void deletePhoneNumber() {
    }

    @Test
    void getAll() {
    }

    @Test
    void existsByNumber() {
    }
}