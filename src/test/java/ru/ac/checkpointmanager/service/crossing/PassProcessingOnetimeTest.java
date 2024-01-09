package ru.ac.checkpointmanager.service.crossing;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ac.checkpointmanager.exception.PassAlreadyUsedException;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.repository.CrossingRepository;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class PassProcessingOnetimeTest {

    @Mock
    CrossingRepository crossingRepository;

    @InjectMocks
    PassProcessingOnetime passProcessingOnetime;

    @Test
    void processPass_ifDirectionOut_setStatusWithoutExceptions() {
        PassAuto passAuto = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(TestUtils.getUser(),
                TestUtils.getTerritory(),
                TestUtils.getCar(TestUtils.getCarBrand()));
        passAuto.setExpectedDirection(Direction.IN);

        passProcessingOnetime.process(passAuto, Direction.OUT);

        Assertions.assertThat(passAuto.getStatus())
                .as("Check if pass status was set to %s".formatted(PassStatus.COMPLETED))
                .isEqualTo(PassStatus.COMPLETED);
    }

    @Test
    void processPass_ifDirectionInAndNoCrossings_passStatusUnchanged() {
        PassAuto passAuto = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(TestUtils.getUser(),
                TestUtils.getTerritory(),
                TestUtils.getCar(TestUtils.getCarBrand()));
        passAuto.setExpectedDirection(Direction.IN);
        Mockito.when(crossingRepository.findCrossingsByPassId(Mockito.any())).thenReturn(Collections.emptyList());

        passProcessingOnetime.process(passAuto, Direction.IN);

        Assertions.assertThat(passAuto.getStatus())
                .as("Check if pass status left unchanged")
                .isEqualTo(PassStatus.ACTIVE);
    }

    @Test
    void processPass_IfDirectionInAndCrossingsExists_throwPassAlreadyUsedException() {
        PassAuto passAuto = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(TestUtils.getUser(),
                TestUtils.getTerritory(),
                TestUtils.getCar(TestUtils.getCarBrand()));
        passAuto.setExpectedDirection(Direction.IN);
        Mockito.when(crossingRepository.findCrossingsByPassId(Mockito.any())).thenReturn(List.of(new Crossing()));

        Assertions.assertThatExceptionOfType(PassAlreadyUsedException.class)
                .as("Check if PassAlreadyUsedException will be thrown")
                .isThrownBy(() -> passProcessingOnetime.process(passAuto, Direction.IN));
    }

}
