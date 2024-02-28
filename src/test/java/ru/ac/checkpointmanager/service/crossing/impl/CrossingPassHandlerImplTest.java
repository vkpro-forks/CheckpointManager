package ru.ac.checkpointmanager.service.crossing.impl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.ac.checkpointmanager.exception.PassProcessorException;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassTimeType;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.service.crossing.PassProcessor;
import ru.ac.checkpointmanager.util.PassTestData;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class CrossingPassHandlerImplTest {

    @Mock
    PassRepository passRepository;

    @Mock
    PassProcessorOnetime passProcessingOnetime;

    @Mock
    PassProcessorPermanent passProcessingPermanent;

    @InjectMocks
    CrossingPassHandlerImpl crossingPassHandler;

    @Captor
    ArgumentCaptor<Pass> passArgumentCaptor;

    @BeforeEach
    void init() {
        Map<String, PassProcessor> passProcessingMap = Map.of(
                "ONETIME", passProcessingOnetime,
                "PERMANENT", passProcessingPermanent
        );
        ReflectionTestUtils.setField(crossingPassHandler, "passProcessingMap", passProcessingMap);
    }

    @ParameterizedTest
    @MethodSource("getTestDirections")
    void handle_OneTimePass_ChangeDirectionAndSave(Direction in, Direction changed) {
        PassAuto passAuto = PassTestData.getSimpleActiveOneTimePassAutoFor3Hours(TestUtils.getUser(),
                TestUtils.getTerritory(),
                TestUtils.getCar(TestUtils.getCarBrand()));
        passAuto.setExpectedDirection(in);

        crossingPassHandler.handle(passAuto, in);

        Mockito.verify(passRepository).save(passArgumentCaptor.capture());
        Pass captured = passArgumentCaptor.getValue();
        Assertions.assertThat(captured.getExpectedDirection())
                .as("Check if pass direction was changed to opposite")
                .isEqualTo(changed);
        Mockito.verify(passProcessingOnetime).process(passAuto, in);
    }

    @ParameterizedTest
    @MethodSource("getTestDirections")
    void handle_PermanentPass_ChangeDirectionAndSave(Direction in, Direction changed) {
        PassAuto passAuto = PassTestData.getSimpleActivePermanentAutoFor3Hours(TestUtils.getUser(),
                TestUtils.getTerritory(),
                TestUtils.getCar(TestUtils.getCarBrand()));
        passAuto.setExpectedDirection(in);

        crossingPassHandler.handle(passAuto, in);

        Mockito.verify(passRepository).save(passArgumentCaptor.capture());
        Pass captured = passArgumentCaptor.getValue();
        Assertions.assertThat(captured.getExpectedDirection())
                .as("Check if pass direction was changed to opposite")
                .isEqualTo(changed);
        Mockito.verify(passProcessingPermanent).process(passAuto, in);
    }

    @Test
    void handle_PassTypeNotInMap_ThrowPassProcessorException() {
        PassAuto passAuto = new PassAuto();
        passAuto.setTimeType(PassTimeType.ONETIME);
        ReflectionTestUtils.setField(crossingPassHandler, "passProcessingMap", Collections.emptyMap());

        Assertions.assertThatExceptionOfType(PassProcessorException.class)
                .as("Check if exception will be thrown, if no suitable processor found in map")
                .isThrownBy(() -> crossingPassHandler.handle(passAuto, Direction.IN));
    }

    private static Stream<Arguments> getTestDirections() {
        return Stream.of(
                Arguments.of(Direction.IN, Direction.OUT),
                Arguments.of(Direction.OUT, Direction.IN)
        );
    }

}
