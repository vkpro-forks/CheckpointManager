package ru.ac.checkpointmanager.extension.argprovider;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.model.passes.PassTimeType;
import ru.ac.checkpointmanager.util.TestUtils;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
public class PassUpdateDTOWithVisitorArgumentProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        log.info("Configuring passUpdateDtos with Visitor for checking how pass will be updated");
        String updatedComment = "my comment";
        String newEmail = "java@java.com";
        PassUpdateDTO passUpdateDTOForSavedVisitor = new PassUpdateDTO(updatedComment, PassTimeType.PERMANENT,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
                new VisitorDTO(TestUtils.getVisitorRandomUUID().getId(), newEmail, TestUtils.PHONE_NUM, "note"), null, UUID.randomUUID());
        PassUpdateDTO passUpdateDTOForNewVisitor = new PassUpdateDTO(updatedComment, PassTimeType.PERMANENT,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
                new VisitorDTO(null, newEmail, TestUtils.PHONE_NUM, "note"), null, UUID.randomUUID());

        return Stream.of(Arguments.of(passUpdateDTOForSavedVisitor),
                Arguments.of(passUpdateDTOForNewVisitor));
    }
}
