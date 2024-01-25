package ru.ac.checkpointmanager.service.event.impl;

import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.ac.checkpointmanager.dto.passes.PagingParams;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.util.TestUtils;

@ExtendWith(MockitoExtension.class)
class PassInOutViewServiceImplTest {

    @Mock
    PassRepository passRepository;

    @Mock
    TerritoryRepository territoryRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    PassInOutViewServiceImpl passInOutViewService;

    PagingParams pagingParams = new PagingParams(0, 100);

    @Test
    void findEventsByUser_AllOk_ReturnPageWithEvents() {
        Mockito.when(userRepository.existsById(Mockito.any())).thenReturn(true);

        passInOutViewService.findEventsByUser(TestUtils.USER_ID, pagingParams);

        Mockito.verify(passRepository, Mockito.times(1))
                .findEventsByUser(TestUtils.USER_ID, PageRequest.of(pagingParams.getPage(), pagingParams.getSize()));
    }

    @Test
    void findEventsByUser_NoUser_ThrowException() {
        Mockito.when(userRepository.existsById(Mockito.any())).thenReturn(false);

        Assertions.assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(() ->
                        passInOutViewService.findEventsByUser(TestUtils.USER_ID, pagingParams))
                .isInstanceOf(EntityNotFoundException.class)
                .withMessage(ExceptionUtils.USER_NOT_FOUND_MSG.formatted(TestUtils.USER_ID));

        Mockito.verify(passRepository, Mockito.never()).findEventsByUser(Mockito.any(), Mockito.any());
    }

    @Test
    void findEventsByTerritory_AllOk_ReturnPageWithEvents() {
        Mockito.when(territoryRepository.existsById(Mockito.any())).thenReturn(true);

        passInOutViewService.findEventsByTerritory(TestUtils.TERR_ID, pagingParams);

        Mockito.verify(passRepository, Mockito.times(1))
                .findEventsByTerritory(TestUtils.TERR_ID, PageRequest.of(pagingParams.getPage(), pagingParams.getSize()));
    }

    @Test
    void findEventsByTerritory_NoUser_ThrowException() {
        Mockito.when(territoryRepository.existsById(Mockito.any())).thenReturn(false);

        Assertions.assertThatExceptionOfType(TerritoryNotFoundException.class).isThrownBy(() ->
                        passInOutViewService.findEventsByTerritory(TestUtils.TERR_ID, pagingParams))
                .isInstanceOf(EntityNotFoundException.class)
                .withMessage(ExceptionUtils.TERRITORY_NOT_FOUND_MSG.formatted(TestUtils.TERR_ID));

        Mockito.verify(passRepository, Mockito.never()).findEventsByTerritory(Mockito.any(), Mockito.any());
    }

}