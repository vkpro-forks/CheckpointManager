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
import ru.ac.checkpointmanager.exception.pass.PassException;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassTimeType;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.service.crossing.PassProcessing;
import ru.ac.checkpointmanager.service.crossing.PassProcessingOnetime;
import ru.ac.checkpointmanager.service.crossing.PassProcessingPermanent;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class PassHandlerImplTest {

    @Mock
    PassRepository passRepository;

    @Mock
    PassProcessingOnetime passProcessingOnetime;

    @Mock
    PassProcessingPermanent passProcessingPermanent;

    @InjectMocks
    PassHandlerImpl passHandler;

    @Captor
    ArgumentCaptor<Pass> passArgumentCaptor;

    @BeforeEach
    void init() {
        Map<String, PassProcessing> passProcessingMap = Map.of(
                "ONETIME", passProcessingOnetime,
                "PERMANENT", passProcessingPermanent
        );
        ReflectionTestUtils.setField(passHandler, "passProcessingMap", passProcessingMap);
    }

    @ParameterizedTest
    @MethodSource("getTestDirections")
    void handle_ifOneTimePass_changeDirectionAndSave(Direction in, Direction changed) {
        PassAuto passAuto = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(TestUtils.getUser(),
                TestUtils.getTerritory(),
                TestUtils.getCar(TestUtils.getCarBrand()));
        passAuto.setExpectedDirection(in);

        passHandler.handle(passAuto, in);

        Mockito.verify(passRepository).save(passArgumentCaptor.capture());
        Pass captured = passArgumentCaptor.getValue();
        Assertions.assertThat(captured.getExpectedDirection())
                .as("Check if pass direction was changed to opposite")
                .isEqualTo(changed);
        Mockito.verify(passProcessingOnetime).process(passAuto, in);
    }

    @ParameterizedTest
    @MethodSource("getTestDirections")
    void handle_ifPermanentPass_changeDirectionAndSave(Direction in, Direction changed) {
        PassAuto passAuto = TestUtils.getSimpleActivePermanentAutoFor3Hours(TestUtils.getUser(),
                TestUtils.getTerritory(),
                TestUtils.getCar(TestUtils.getCarBrand()));
        passAuto.setExpectedDirection(in);

        passHandler.handle(passAuto, in);

        Mockito.verify(passRepository).save(passArgumentCaptor.capture());
        Pass captured = passArgumentCaptor.getValue();
        Assertions.assertThat(captured.getExpectedDirection())
                .as("Check if pass direction was changed to opposite")
                .isEqualTo(changed);
        Mockito.verify(passProcessingPermanent).process(passAuto, in);
    }

    @Test
    void handle_ifPassTypeNotInMap_throwException() {
        PassAuto passAuto = new PassAuto();

        passAuto.setTimeType(PassTimeType.ONETIME);
        ReflectionTestUtils.setField(passHandler, "passProcessingMap", Collections.emptyMap());

        Assertions.assertThatExceptionOfType(PassException.class)
                .as("Check if exception will be thrown, if no suitable processor found in map")
                .isThrownBy(() -> passHandler.handle(passAuto, Direction.IN));
    }

    private static Stream<Arguments> getTestDirections() {
        return Stream.of(
                Arguments.of(Direction.IN, Direction.OUT),
                Arguments.of(Direction.OUT, Direction.IN)
        );
    }

}
