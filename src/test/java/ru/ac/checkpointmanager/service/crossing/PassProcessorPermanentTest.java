package ru.ac.checkpointmanager.service.crossing;

import ch.qos.logback.classic.Level;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.ac.checkpointmanager.ext.LoggingMemoryAppenderTestResolver;
import ru.ac.checkpointmanager.ext.MemoryAppender;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.service.crossing.impl.PassProcessorPermanent;
import ru.ac.checkpointmanager.util.TestUtils;

@ExtendWith(LoggingMemoryAppenderTestResolver.class)
class PassProcessorPermanentTest {

    PassProcessorPermanent passProcessingPermanent = new PassProcessorPermanent();

    MemoryAppender memoryAppender;

    @BeforeEach
    public void init(MemoryAppender memoryAppender) {
        this.memoryAppender = memoryAppender;
        memoryAppender.start();
    }

    @Test
    void process_DirectionIn_noExceptionsAndOneDebugLog() {
        PassAuto passAuto = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(TestUtils.getUser(),
                TestUtils.getTerritory(),
                TestUtils.getCar(TestUtils.getCarBrand()));
        passAuto.setExpectedDirection(Direction.IN);

        Assertions.assertThatNoException().as("Check if no exceptions thrown")
                .isThrownBy(
                        () -> passProcessingPermanent.process(passAuto, Direction.IN)
                );

        Assertions.assertThat(memoryAppender.contains("Processing permanent pass", Level.DEBUG))
                .as("Check if DEBUG log with processing msg exists")
                .isTrue();
        Assertions.assertThat(memoryAppender.contains("Two crossings in", Level.WARN))
                .as("Check if WARN log msg doesn't exist")
                .isFalse();
    }

    @Test
    void process_DirectionOut_noExceptionsOneDebugLogAndWarnLog() {
        PassAuto passAuto = TestUtils.getSimpleActiveOneTimePassAutoFor3Hours(TestUtils.getUser(),
                TestUtils.getTerritory(),
                TestUtils.getCar(TestUtils.getCarBrand()));
        passAuto.setExpectedDirection(Direction.IN);

        Assertions.assertThatNoException().as("Check if no exceptions thrown")
                .isThrownBy(
                        () -> passProcessingPermanent.process(passAuto, Direction.OUT)
                );

        Assertions.assertThat(memoryAppender.contains("Processing permanent pass", Level.DEBUG))
                .as("Check if DEBUG log with processing msg exists")
                .isTrue();
        Assertions.assertThat(memoryAppender.contains("Two crossings in", Level.WARN))
                .as("Check if WARN log msg exists")
                .isTrue();
    }

}
