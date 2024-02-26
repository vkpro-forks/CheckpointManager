package ru.ac.checkpointmanager.ext.argprovider;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassWalk;
import ru.ac.checkpointmanager.specification.model.PassAuto_;
import ru.ac.checkpointmanager.specification.model.PassWalk_;
import ru.ac.checkpointmanager.util.PassTestData;

import java.util.stream.Stream;

@Slf4j
public class PassArgumentsForExceptionInPassResolver implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        log.info("Configuring arguments with passes and passUpdateDtos for checking exceptions");
        PassAuto passAuto = new PassAuto();
        passAuto.setDtype(PassAuto_.DTYPE);
        PassWalk passWalk = new PassWalk();
        passWalk.setDtype(PassWalk_.DTYPE);
        return Stream.of(
                Arguments.of(passAuto, PassTestData.getPassUpdateDTOVisitor(), ExceptionUtils.CHANGE_PASS_TYPE_TO_WALK),
                Arguments.of(passWalk, PassTestData.getPassUpdateDTOWithCar(), ExceptionUtils.CHANGE_PASS_TYPE_TO_AUTO)
        );
    }
}
