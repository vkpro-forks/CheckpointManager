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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.ac.checkpointmanager.assertion.PassAssert;
import ru.ac.checkpointmanager.dto.passes.PassFilterParams;
import ru.ac.checkpointmanager.dto.passes.PagingParams;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.mapper.PassMapper;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.repository.CrossingRepository;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.service.passes.impl.PassServiceImpl;
import ru.ac.checkpointmanager.service.territories.TerritoryService;
import ru.ac.checkpointmanager.service.user.UserService;
import ru.ac.checkpointmanager.util.PassTestData;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.Collections;
import java.util.Optional;
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
    PassResolver passResolver;

    @Mock
    PassChecker passChecker;

    @Mock
    PassMapper passMapper;

    @InjectMocks
    PassServiceImpl passService;

    @Captor
    ArgumentCaptor<Pass> passArgumentCaptor;

    @Test
    void addPass_AllOk_SaveAndReturn() {
        PassCreateDTO passCreateDTO = PassTestData.getPassCreateDTOWithCar();
        UUID userId = passCreateDTO.getUserId();
        User user = TestUtils.getUser();
        user.setId(TestUtils.USER_ID);
        Mockito.when(passRepository.findAllPassesByUserId(userId)).thenReturn(Collections.emptyList());
        Pass pass = new PassAuto();
        pass.setTerritory(TestUtils.getTerritory());
        pass.setId(PassTestData.PASS_ID);
        pass.setUser(user);
        pass.setStartTime(passCreateDTO.getStartTime());
        pass.setEndTime(passCreateDTO.getEndTime());
        pass.setComment(passCreateDTO.getComment());
        Mockito.when(passResolver.createPass(passCreateDTO)).thenReturn(pass);

        passService.addPass(passCreateDTO);

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

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void findPasses_AllOk_ReturnPageWithPasses() {
        PagingParams pagingParams = new PagingParams(0, 100);
        PassFilterParams passFilterParams = new PassFilterParams(null, null, null, null);
        Page mockPage = Mockito.mock(Page.class);
        Mockito.when(passRepository.findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class)))
                .thenReturn(mockPage);

        passService.findPasses(pagingParams, passFilterParams, "part");

        Mockito.verify(passRepository, Mockito.times(1)).findAll(Mockito.any(Specification.class), Mockito.any(Pageable.class));
    }

    @Test
    void updatePass_AllOk_UpdatePassAndSave() {
        PassAuto passAuto = PassTestData.getSimpleActiveOneTimePassAutoFor3Hours(new User(), null, null);
        Mockito.when(passRepository.findById(Mockito.any())).thenReturn(Optional.of(passAuto));
        PassUpdateDTO passUpdateDTO = PassTestData.getPassUpdateDTOWithCar();
        Mockito.when(passResolver.updatePass(passUpdateDTO, passAuto)).thenReturn(passAuto);

        passService.updatePass(passUpdateDTO);

        Mockito.verify(passRepository).save(passArgumentCaptor.capture());
        PassAssert.assertThat(passArgumentCaptor.getValue()).isPassFieldsMatches(
                passUpdateDTO.getComment(), passUpdateDTO.getStartTime(), passUpdateDTO.getEndTime(),
                passUpdateDTO.getTimeType()
        );
    }

    @Test
    void updatePass_AllOkNoCommentInDto_UpdatePassAndSave() {
        PassAuto passAuto = PassTestData.getSimpleActiveOneTimePassAutoFor3Hours(new User(), null, null);
        Mockito.when(passRepository.findById(Mockito.any())).thenReturn(Optional.of(passAuto));
        PassUpdateDTO passUpdateDTO = PassTestData.getPassUpdateDTOWithCar();
        passUpdateDTO.setComment(null);
        Mockito.when(passResolver.updatePass(passUpdateDTO, passAuto)).thenReturn(passAuto);

        passService.updatePass(passUpdateDTO);

        Mockito.verify(passRepository).save(passArgumentCaptor.capture());
        PassAssert.assertThat(passArgumentCaptor.getValue()).isPassFieldsMatches(
                passAuto.getComment(), passUpdateDTO.getStartTime(), passUpdateDTO.getEndTime(),
                passUpdateDTO.getTimeType()
        );
    }
}
