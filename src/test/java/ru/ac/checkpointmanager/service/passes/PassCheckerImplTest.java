package ru.ac.checkpointmanager.service.passes;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ac.checkpointmanager.exception.pass.PassException;
import ru.ac.checkpointmanager.exception.pass.UserTerritoryRelationException;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class PassCheckerImplTest {

    @Mock
    PassRepository passRepository;

    @InjectMocks
    PassCheckerImpl passChecker;

    @Test
    void shouldValidateIfRelationExists() {
        Mockito.when(passRepository.checkUserTerritoryRelation(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        Assertions.assertThatNoException().isThrownBy(() ->
                passChecker.checkUserTerritoryRelation(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void shouldThrowExceptionIfRelationDoesntExists() {
        Mockito.when(passRepository.checkUserTerritoryRelation(TestUtils.USER_ID, TestUtils.TERR_ID)).thenReturn(false);

        Assertions.assertThatThrownBy(() ->
                        passChecker.checkUserTerritoryRelation(TestUtils.USER_ID, TestUtils.TERR_ID))
                .isInstanceOf(UserTerritoryRelationException.class)
                .isInstanceOf(PassException.class);
    }

}
