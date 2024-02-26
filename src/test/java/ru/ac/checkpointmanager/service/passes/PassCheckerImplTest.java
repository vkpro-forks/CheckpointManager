package ru.ac.checkpointmanager.service.passes;

import ch.qos.logback.classic.Level;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.MismatchedTerritoryException;
import ru.ac.checkpointmanager.exception.pass.InactivePassException;
import ru.ac.checkpointmanager.exception.pass.ModifyPassException;
import ru.ac.checkpointmanager.exception.pass.PassException;
import ru.ac.checkpointmanager.exception.pass.UserTerritoryRelationException;
import ru.ac.checkpointmanager.ext.LoggingMemoryAppenderTestResolver;
import ru.ac.checkpointmanager.ext.MemoryAppender;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.checkpoints.CheckpointType;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.service.passes.impl.PassCheckerImpl;
import ru.ac.checkpointmanager.util.PassTestData;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.UUID;
import java.util.stream.Stream;

@ExtendWith({MockitoExtension.class, LoggingMemoryAppenderTestResolver.class})
class PassCheckerImplTest {

    @Mock
    PassRepository passRepository;

    @InjectMocks
    PassCheckerImpl passChecker;

    MemoryAppender memoryAppender;

    @BeforeEach
    void init(MemoryAppender memoryAppender) {
        this.memoryAppender = memoryAppender;
    }

    @Test
    void checkUserTerritoryRelation_RelationExists_NoException() {
        Mockito.when(passRepository.checkUserTerritoryRelation(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        Assertions.assertThatNoException().isThrownBy(() ->
                passChecker.checkUserTerritoryRelation(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void checkUserTerritoryRelation_RelationDoesntExists_ThrowException() {
        Mockito.when(passRepository.checkUserTerritoryRelation(TestUtils.USER_ID, TestUtils.TERR_ID)).thenReturn(false);

        Assertions.assertThatThrownBy(() ->
                        passChecker.checkUserTerritoryRelation(TestUtils.USER_ID, TestUtils.TERR_ID))
                .isInstanceOf(UserTerritoryRelationException.class)
                .isInstanceOf(PassException.class);
    }

    @Test
    void checkPassActivity_PassIsActive_NoException() {
        PassAuto passAuto = new PassAuto();
        passAuto.setStatus(PassStatus.ACTIVE);

        Assertions.assertThatNoException().as("Check if no exception thrown if Pass is active")
                .isThrownBy(() -> passChecker.checkPassActivity(passAuto));
    }

    @ParameterizedTest
    @EnumSource(value = PassStatus.class, names = {"ACTIVE"}, mode = EnumSource.Mode.EXCLUDE)
    void checkPassActivity_PassInactive_ThrowException(PassStatus status) {
        PassAuto passAuto = new PassAuto();
        passAuto.setStatus(status);

        Assertions.assertThatExceptionOfType(InactivePassException.class)
                .as("Check if exception will be thrown if pass not in ACTIVE status")
                .isThrownBy(() -> passChecker.checkPassActivity(passAuto))
                .isInstanceOf(PassException.class);
    }

    @Test
    void checkPassAndCheckpointTerritories_BothHaveSameTerritory_NoException() {
        Territory territory = TestUtils.getTerritory();
        PassAuto passAuto = new PassAuto();
        passAuto.setTerritory(territory);
        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.UNIVERSAL, territory);

        Assertions.assertThatNoException().isThrownBy(
                () -> passChecker.checkPassAndCheckpointTerritories(passAuto, checkpoint));
    }

    @ParameterizedTest
    @MethodSource("getTerritoriesForCheckpointAndPass")
    void checkPassAndCheckpointTerritories_PassTerrDifferentFromCheckpoint_ThrowMismatchedTerritoryException(
            Territory territory, Territory anotherTerritory) {
        PassAuto passAuto = new PassAuto();
        passAuto.setTerritory(territory);
        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.UNIVERSAL, anotherTerritory);

        Assertions.assertThatExceptionOfType(MismatchedTerritoryException.class)
                .as("Check if MismatchedTerritoryException will be thrown")
                .isThrownBy(() -> passChecker.checkPassAndCheckpointTerritories(passAuto, checkpoint))
                .isInstanceOf(PassException.class);
    }

    @Test
    void checkPassAndCheckpointCompatibility_PassTypeIncompatibleWithCheckpointType_WriteLog() {
        memoryAppender.start();
        PassAuto passAuto = new PassAuto();
        passAuto.setDtype("AUTO");
        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.UNIVERSAL, null);
        checkpoint.setType(CheckpointType.WALK);

        passChecker.checkPassAndCheckpointCompatibility(passAuto, checkpoint);

        Assertions.assertThat(memoryAppender.contains("Conflict between the types", Level.WARN))
                .as("Check if case is logged with WARN")
                .isTrue();
    }

    @Test
    void checkPassAndCheckpointCompatibility_PassTypeCompatibleWithCheckpointType_DontWriteLog() {
        memoryAppender.start();
        PassAuto passAuto = new PassAuto();
        passAuto.setDtype("AUTO");
        Checkpoint checkpoint = TestUtils.getCheckpoint(CheckpointType.UNIVERSAL, null);
        checkpoint.setType(CheckpointType.AUTO);

        passChecker.checkPassAndCheckpointCompatibility(passAuto, checkpoint);

        Assertions.assertThat(!memoryAppender.contains("Conflict between the types", Level.WARN))
                .as("Check standard flow without WARN")
                .isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = PassStatus.class, names = {"ACTIVE", "DELAYED"}, mode = EnumSource.Mode.INCLUDE)
    void isPassUpdatable_NoExceptions(PassStatus passStatus) {
        PassAuto passAuto = new PassAuto();
        passAuto.setStatus(passStatus);

        Assertions.assertThatNoException().isThrownBy(() -> passChecker.isPassUpdatable(passAuto));
    }

    @ParameterizedTest
    @EnumSource(value = PassStatus.class, names = {"ACTIVE", "DELAYED"}, mode = EnumSource.Mode.EXCLUDE)
    void isPassUpdatable_ThrowException(PassStatus passStatus) {
        PassAuto passAuto = new PassAuto();
        passAuto.setId(PassTestData.PASS_ID);
        passAuto.setStatus(passStatus);

        Assertions.assertThatExceptionOfType(ModifyPassException.class)
                .isThrownBy(() -> passChecker.isPassUpdatable(passAuto))
                .withMessage(ExceptionUtils.PASS_NOT_UPDATE.formatted(PassTestData.PASS_ID, passStatus.name()))
                .isInstanceOf(PassException.class);
    }

    private static Stream<Arguments> getTerritoriesForCheckpointAndPass() {
        Territory territory = TestUtils.getTerritory();
        Territory anotherTerritory = new Territory();
        anotherTerritory.setId(UUID.randomUUID());
        return Stream.of(
                Arguments.of(territory, anotherTerritory),
                Arguments.of(territory, null),
                Arguments.of(null, territory)
        );
    }

}
