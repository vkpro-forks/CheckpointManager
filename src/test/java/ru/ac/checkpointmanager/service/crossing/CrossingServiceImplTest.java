package ru.ac.checkpointmanager.service.crossing;

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
import ru.ac.checkpointmanager.extension.LoggingMemoryAppenderTestResolver;
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
import ru.ac.checkpointmanager.service.passes.PassChecker;
import ru.ac.checkpointmanager.service.passes.PassService;
import ru.ac.checkpointmanager.util.PassTestData;
import ru.ac.checkpointmanager.util.TestUtils;

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

    @Mock
    PassChecker passChecker;

    @InjectMocks
    CrossingServiceImpl crossingService;

    @Captor
    ArgumentCaptor<Crossing> crossingArgumentCaptor;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(crossingService, "mapper", new CrossingMapper(new ModelMapper()));
    }

    @Test
    void addCrossing_PassActiveCompatibleWithCheckpointSameTerritories_SaveCrossing() {
        CrossingRequestDTO crossingRequestDTO = TestUtils.getCrossingRequestDTO();
        PassAuto passAuto = new PassAuto();
        passAuto.setStatus(PassStatus.ACTIVE);
        Territory territory = TestUtils.getTerritory();
        passAuto.setTerritory(territory);
        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.UNIVERSAL, territory);
        Mockito.when(passService.findPassById(PassTestData.PASS_ID))
                .thenReturn(passAuto);
        Mockito.when(checkpointService.findCheckpointById(TestUtils.CHECKPOINT_ID))
                .thenReturn(checkpoint);
        Mockito.when(crossingRepository.save(Mockito.any()))
                .thenReturn(TestUtils.getCrossing(passAuto, checkpoint, Direction.IN));

        crossingService.addCrossing(crossingRequestDTO, Direction.IN);
        Mockito.verify(passChecker).checkPassActivity(passAuto);
        Mockito.verify(passChecker).checkPassAndCheckpointCompatibility(passAuto, checkpoint);
        Mockito.verify(passChecker).checkPassAndCheckpointTerritories(passAuto, checkpoint);
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

}
