package ru.ac.checkpointmanager.service.passes;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.mapper.PassMapper;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.repository.CrossingRepository;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.service.territories.TerritoryService;
import ru.ac.checkpointmanager.service.user.UserService;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.Collections;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class PassServiceImplTest {

    @Mock
    PassRepository passRepository;

    @Mock
    CrossingRepository crossingRepository;

    @Mock
    UserService userService;

    @Mock
    TerritoryService territoryService;

    @Mock
    PassMapper passMapper;

    @InjectMocks
    PassServiceImpl passService;

    @Captor
    ArgumentCaptor<Pass> passArgumentCaptor;


    @Test
    void shouldAddPass() {
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTO();
        UUID userId = passCreateDTO.getUserId();
        UUID territoryId = passCreateDTO.getTerritoryId();
        User mockUser = Mockito.mock(User.class);
        Mockito.when(mockUser.getId()).thenReturn(userId);
        Mockito.when(userService.findUserById(userId)).thenReturn(mockUser);
        Territory mockTerritory = Mockito.mock(Territory.class);
        Mockito.when(mockTerritory.getId()).thenReturn(territoryId);
        Mockito.when(territoryService.findTerritoryById(territoryId)).thenReturn(mockTerritory);
        Mockito.when(passRepository.checkUserTerritoryRelation(userId, territoryId)).thenReturn(true);
        Mockito.when(passRepository.findAllPassesByUserId(userId)).thenReturn(Collections.emptyList());
        Pass pass = new PassAuto();
        pass.setTerritory(mockTerritory);
        pass.setUser(mockUser);
        pass.setStartTime(passCreateDTO.getStartTime());
        pass.setEndTime(passCreateDTO.getEndTime());
        pass.setComment(passCreateDTO.getComment());
        Mockito.when(passMapper.toPass(passCreateDTO)).thenReturn(pass);//все таки у маппера есть логика,
        //в зависимости от визитора или кара он создает либо пеший либо авто пропуск
        // поэтому пока он не оттестирован, лучше замокать

        passService.addPass(passCreateDTO);

        Mockito.verify(userService).findUserById(userId);
        Mockito.verify(territoryService).findTerritoryById(territoryId);
        Mockito.verify(passRepository).checkUserTerritoryRelation(userId, territoryId);
        Mockito.verify(passRepository).findAllPassesByUserId(userId);
        Mockito.verify(passRepository).save(passArgumentCaptor.capture());

        Pass captured = passArgumentCaptor.getValue();
        //просто проверяем что не трогали поля
        Assertions.assertThat(captured.getStartTime()).isEqualTo(pass.getStartTime());
        Assertions.assertThat(captured.getEndTime()).isEqualTo(passCreateDTO.getEndTime());
        //коммент у нас был, т.е. + 1 тест на проверку формирования комента, копипаст + небольшие изменения
        Assertions.assertThat(captured.getComment()).isEqualTo(passCreateDTO.getComment());
        //тут проверяем поля которые установили
        Assertions.assertThat(captured.getStatus()).isEqualTo(PassStatus.DELAYED);
        Assertions.assertThat(captured.getDtype()).isEqualTo(pass.getDtype());//проверка типа пропуска
        Assertions.assertThat(captured.getId()).isNotNull();
    }

}
