package ru.ac.checkpointmanager.service.territories;

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
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.exception.UserTerritoryRelationException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.mapper.TerritoryMapper;
import ru.ac.checkpointmanager.mapper.UserMapper;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TerritoryServiceImplTest {

    @Mock
    TerritoryRepository territoryRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    TerritoryServiceImpl territoryService;

    @Captor
    ArgumentCaptor<Territory> territoryArgumentCaptor;

    @Captor
    ArgumentCaptor<User> userArgumentCaptor;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(territoryService, "territoryMapper", new TerritoryMapper(new ModelMapper()));
        ReflectionTestUtils.setField(territoryService, "userMapper", new UserMapper(new ModelMapper()));
    }

    @Test
    void shouldAddTerritory() {
        TerritoryDTO territoryDTO = TestUtils.getTerritoryDTO();
        Mockito.when(territoryRepository.save(Mockito.any())).thenReturn(TestUtils.getTerritory());

        territoryService.addTerritory(territoryDTO);

        Mockito.verify(territoryRepository).save(territoryArgumentCaptor.capture());
        Territory captured = territoryArgumentCaptor.getValue();
        Assertions.assertThat(captured.getId()).isEqualTo(territoryDTO.getId());
        Assertions.assertThat(captured.getName()).isEqualTo(territoryDTO.getName().replaceAll("\\s+", " ").trim());
        Assertions.assertThat(captured.getNote()).isEqualTo(territoryDTO.getNote().replaceAll("\\s+", " ").trim());
    }

    @Test
    void shouldDeleteTerritory() {
        Mockito.when(territoryRepository.findById(Mockito.any())).thenReturn(Optional.of(TestUtils.getTerritory()));

        territoryService.deleteTerritoryById(TestUtils.TERR_ID);

        Mockito.verify(territoryRepository).deleteById(TestUtils.TERR_ID);
    }

    @Test
    void shouldThrowExceptionIfTerritoryForDeleteNotFound() {
        Mockito.when(territoryRepository.findById(Mockito.any())).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> territoryService.deleteTerritoryById(TestUtils.TERR_ID))
                .isInstanceOf(TerritoryNotFoundException.class);

        Mockito.verify(territoryRepository, Mockito.never()).deleteById(TestUtils.TERR_ID);
    }

    @Test
    void shouldAttachTerritoryToUser() {
        Mockito.when(territoryRepository.findById(TestUtils.TERR_ID)).thenReturn(Optional.of(TestUtils.getTerritory()));
        User user = TestUtils.getUser();
        user.setId(TestUtils.USER_ID);
        Mockito.when(userRepository.findById(TestUtils.USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(territoryRepository.checkUserTerritoryRelation(TestUtils.USER_ID, TestUtils.TERR_ID))
                .thenReturn(false);

        territoryService.attachUserToTerritory(TestUtils.TERR_ID, TestUtils.USER_ID);

        Mockito.verify(userRepository).save(userArgumentCaptor.capture());
        User captured = userArgumentCaptor.getValue();

        Assertions.assertThat(captured.getTerritories()).as("Check if captured user has attached territory")
                .hasSize(1).contains(TestUtils.getTerritory());
    }

    @Test
    void shouldThrowUserNotFoundExceptionForAttachUserToTerritory() {
        Mockito.when(territoryRepository.findById(TestUtils.TERR_ID)).thenReturn(Optional.of(TestUtils.getTerritory()));
        Mockito.when(userRepository.findById(TestUtils.USER_ID)).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(UserNotFoundException.class)
                .as("Check if UserNotFoundException exception throws")
                .isThrownBy(() -> territoryService.attachUserToTerritory(TestUtils.TERR_ID, TestUtils.USER_ID));

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(territoryRepository, Mockito.never()).checkUserTerritoryRelation(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowTerritoryNotFoundExceptionForAttachTerritoryToUser() {
        Mockito.when(territoryRepository.findById(TestUtils.TERR_ID)).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(TerritoryNotFoundException.class)
                .as("Check if TerritoryNotFound exception throws")
                .isThrownBy(() -> territoryService.attachUserToTerritory(TestUtils.TERR_ID, TestUtils.USER_ID));

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(territoryRepository, Mockito.never()).checkUserTerritoryRelation(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowUserAlreadyConnectedToTerritoryExceptionForAttachTerritoryToUser() {
        Mockito.when(territoryRepository.findById(TestUtils.TERR_ID)).thenReturn(Optional.of(TestUtils.getTerritory()));
        User user = TestUtils.getUser();
        user.setId(TestUtils.USER_ID);
        Mockito.when(userRepository.findById(TestUtils.USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(territoryRepository.checkUserTerritoryRelation(TestUtils.USER_ID, TestUtils.TERR_ID))
                .thenReturn(true);

        Assertions.assertThatExceptionOfType(UserTerritoryRelationException.class)
                .as("Check if UserTerritoryRelation exception throws")
                .isThrownBy(() -> territoryService.attachUserToTerritory(TestUtils.TERR_ID, TestUtils.USER_ID));

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void shouldDetachTerritoryToUser() {
        Mockito.when(territoryRepository.findById(TestUtils.TERR_ID)).thenReturn(Optional.of(TestUtils.getTerritory()));
        User user = TestUtils.getUser();
        user.setId(TestUtils.USER_ID);
        user.getTerritories().add(TestUtils.getTerritory());
        Mockito.when(userRepository.findById(TestUtils.USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(territoryRepository.checkUserTerritoryRelation(TestUtils.USER_ID, TestUtils.TERR_ID))
                .thenReturn(true);

        territoryService.detachUserFromTerritory(TestUtils.TERR_ID, TestUtils.USER_ID);

        Mockito.verify(userRepository).save(userArgumentCaptor.capture());
        User captured = userArgumentCaptor.getValue();

        Assertions.assertThat(captured.getTerritories()).as("Check if captured user hasn't territories")
                .isEmpty();
    }

    @Test
    void shouldThrowUserNotFoundExceptionForDetachUserToTerritory() {
        Mockito.when(territoryRepository.findById(TestUtils.TERR_ID)).thenReturn(Optional.of(TestUtils.getTerritory()));
        Mockito.when(userRepository.findById(TestUtils.USER_ID)).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(UserNotFoundException.class)
                .as("Check if UserNotFoundException exception throws")
                .isThrownBy(() -> territoryService.detachUserFromTerritory(TestUtils.TERR_ID, TestUtils.USER_ID));

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(territoryRepository, Mockito.never()).checkUserTerritoryRelation(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowTerritoryNotFoundExceptionForDetachTerritoryToUser() {
        Mockito.when(territoryRepository.findById(TestUtils.TERR_ID)).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(TerritoryNotFoundException.class)
                .as("Check if TerritoryNotFound exception throws")
                .isThrownBy(() -> territoryService.detachUserFromTerritory(TestUtils.TERR_ID, TestUtils.USER_ID));

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(territoryRepository, Mockito.never()).checkUserTerritoryRelation(Mockito.any(), Mockito.any());
    }

    @Test
    void shouldThrowUserAlreadyConnectedToTerritoryExceptionForDetachTerritoryToUser() {
        Mockito.when(territoryRepository.findById(TestUtils.TERR_ID)).thenReturn(Optional.of(TestUtils.getTerritory()));
        User user = TestUtils.getUser();
        user.setId(TestUtils.USER_ID);
        Mockito.when(userRepository.findById(TestUtils.USER_ID)).thenReturn(Optional.of(user));
        Mockito.when(territoryRepository.checkUserTerritoryRelation(TestUtils.USER_ID, TestUtils.TERR_ID))
                .thenReturn(false);

        Assertions.assertThatExceptionOfType(UserTerritoryRelationException.class)
                .as("Check if UserTerritoryRelation exception throws")
                .isThrownBy(() -> territoryService.detachUserFromTerritory(TestUtils.TERR_ID, TestUtils.USER_ID));

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

}