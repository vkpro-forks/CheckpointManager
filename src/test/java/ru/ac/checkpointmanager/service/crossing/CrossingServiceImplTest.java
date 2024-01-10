package ru.ac.checkpointmanager.service.crossing;

import ch.qos.logback.classic.Level;
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
import ru.ac.checkpointmanager.dto.CrossingRequestDTO;
import ru.ac.checkpointmanager.exception.MismatchedTerritoryException;
import ru.ac.checkpointmanager.exception.pass.InactivePassException;
import ru.ac.checkpointmanager.exception.pass.PassException;
import ru.ac.checkpointmanager.ext.LoggingMemoryAppenderTestResolver;
import ru.ac.checkpointmanager.ext.MemoryAppender;
import ru.ac.checkpointmanager.mapper.CrossingMapper;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.checkpoints.CheckpointType;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.repository.CrossingRepository;
import ru.ac.checkpointmanager.service.checkpoints.CheckpointService;
import ru.ac.checkpointmanager.service.crossing.impl.CrossingServiceImpl;
import ru.ac.checkpointmanager.service.passes.PassService;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.UUID;

@ExtendWith({MockitoExtension.class, LoggingMemoryAppenderTestResolver.class})
class CrossingServiceImplTest {

    @Mock
    CrossingRepository crossingRepository;

    @Mock
    PassService passService;

    @Mock
    CheckpointService checkpointService;

    @Mock
    CrossingPassHandler crossingPassHandler;

    @InjectMocks
    CrossingServiceImpl crossingService;

    @Captor
    ArgumentCaptor<Crossing> crossingArgumentCaptor;

    MemoryAppender memoryAppender;

    @BeforeEach
    void init(MemoryAppender memoryAppender) {
        this.memoryAppender = memoryAppender;
        ReflectionTestUtils.setField(crossingService, "mapper", new CrossingMapper(new ModelMapper()));
    }

    @Test
    void addCrossing_PassActiveCompatibleWithCheckpointSameTerritories_SaveCrossing() { // MethodName_StateUnderTest_ExpectedBehavior
        CrossingRequestDTO crossingRequestDTO = TestUtils.getCrossingRequestDTO();
        PassAuto passAuto = new PassAuto();
        passAuto.setStatus(PassStatus.ACTIVE);
        Territory territory = TestUtils.getTerritory();
        passAuto.setTerritory(territory);
        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.UNIVERSAL, territory);
        Mockito.when(passService.findPassById(TestUtils.PASS_ID))
                .thenReturn(passAuto);
        Mockito.when(checkpointService.findCheckpointById(TestUtils.CHECKPOINT_ID))
                .thenReturn(checkpoint);
        Mockito.when(crossingRepository.save(Mockito.any()))
                .thenReturn(TestUtils.getCrossing(passAuto, checkpoint, Direction.IN));

        crossingService.addCrossing(crossingRequestDTO, Direction.IN);

        Mockito.verify(crossingPassHandler).handle(passAuto, Direction.IN);
        Mockito.verify(crossingRepository).save(crossingArgumentCaptor.capture());
        Crossing captured = crossingArgumentCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertAll(
                () -> Assertions.assertThat(captured.getDirection())
                        .as("Check if direction passed correct")
                        .isEqualTo(Direction.IN),
                () -> Assertions.assertThat(captured.getPass()).as("Check if pass attached to crossing")
                        .isEqualTo(passAuto),
                () -> Assertions.assertThat(captured.getCheckpoint()).as("Check if checkpoint attached to crossing")
                        .isEqualTo(checkpoint),
                () -> Assertions.assertThat(captured.getPerformedAt()).as("Check if performedAt attached to crossing")
                        .isEqualTo(crossingRequestDTO.getPerformedAt())
        );
    }

    @Test
    void addCrossing_PassInactive_ThrowInactivePassException() {
        CrossingRequestDTO crossingRequestDTO = TestUtils.getCrossingRequestDTO();
        PassAuto passAuto = new PassAuto();
        passAuto.setStatus(PassStatus.COMPLETED);
        Territory territory = TestUtils.getTerritory();
        passAuto.setTerritory(territory);
        Mockito.when(passService.findPassById(TestUtils.PASS_ID))
                .thenReturn(passAuto);

        Assertions.assertThatExceptionOfType(InactivePassException.class)
                .as("Check if InactivePassException will be thrown")
                .isThrownBy(() -> crossingService.addCrossing(crossingRequestDTO, Direction.IN))
                .isInstanceOf(PassException.class);

        Mockito.verifyNoInteractions(crossingPassHandler, crossingRepository, crossingRepository);
    }

    @Test
    void addCrossing_PassActiveWithDifferentFromCheckpointTerritory_ThrowMismatchedTerritoryException() {
        CrossingRequestDTO crossingRequestDTO = TestUtils.getCrossingRequestDTO();
        PassAuto passAuto = new PassAuto();
        passAuto.setStatus(PassStatus.ACTIVE);
        Territory territory = TestUtils.getTerritory();
        passAuto.setTerritory(territory);
        Territory anotherTerritory = new Territory();
        anotherTerritory.setId(UUID.randomUUID());
        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.UNIVERSAL, anotherTerritory);
        Mockito.when(passService.findPassById(TestUtils.PASS_ID))
                .thenReturn(passAuto);
        Mockito.when(checkpointService.findCheckpointById(TestUtils.CHECKPOINT_ID))
                .thenReturn(checkpoint);

        Assertions.assertThatExceptionOfType(MismatchedTerritoryException.class)
                .as("Check if MismatchedTerritoryException will be thrown")
                .isThrownBy(() -> crossingService.addCrossing(crossingRequestDTO, Direction.IN))
                .isInstanceOf(PassException.class);

        Mockito.verifyNoInteractions(crossingPassHandler, crossingRepository);
    }

    @Test
    void addCrossing_PassTypeIncompatibleWithCheckpointType() {
        memoryAppender.start();
        CrossingRequestDTO crossingRequestDTO = TestUtils.getCrossingRequestDTO();
        PassAuto passAuto = new PassAuto();
        passAuto.setId(TestUtils.PASS_ID);
        passAuto.setStatus(PassStatus.ACTIVE);
        passAuto.setDtype("AUTO");
        Territory territory = TestUtils.getTerritory();
        passAuto.setTerritory(territory);
        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.UNIVERSAL, territory);
        Mockito.when(passService.findPassById(TestUtils.PASS_ID))
                .thenReturn(passAuto);
        Mockito.when(checkpointService.findCheckpointById(TestUtils.CHECKPOINT_ID))
                .thenReturn(checkpoint);
        Mockito.when(crossingRepository.save(Mockito.any()))
                .thenReturn(TestUtils.getCrossing(passAuto, checkpoint, Direction.IN));
        checkpoint.setType(CheckpointType.WALK);

        crossingService.addCrossing(crossingRequestDTO, Direction.IN);

        Assertions.assertThat(memoryAppender.contains("Conflict between the types", Level.WARN))
                .as("Check if case is logged with WARN")
                .isTrue();
        Mockito.verify(crossingPassHandler).handle(passAuto, Direction.IN);
        Mockito.verify(crossingRepository).save(Mockito.any());

    }

}
