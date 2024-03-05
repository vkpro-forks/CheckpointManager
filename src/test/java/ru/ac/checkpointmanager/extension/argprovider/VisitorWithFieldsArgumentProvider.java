package ru.ac.checkpointmanager.extension.argprovider;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.stream.Stream;

@Slf4j
public class VisitorWithFieldsArgumentProvider implements ArgumentsProvider {

    private static final String NOTE = "note";

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        log.info("Configure arguments for VisitorDto");
        VisitorDTO visitorDTO = TestUtils.getVisitorDTO();
        VisitorDTO visitorDTONullName = new VisitorDTO(null, null, TestUtils.PHONE_NUM, NOTE);
        VisitorDTO visitorDTONullPhone = new VisitorDTO(null, TestUtils.USER_NAME, null, NOTE);
        VisitorDTO visitorDTONullNote = new VisitorDTO(null, TestUtils.USER_NAME, TestUtils.PHONE_NUM, null);
        return Stream.of(
                Arguments.of(visitorDTO, Triple.of(visitorDTO.getName(), visitorDTO.getPhone(), visitorDTO.getNote())),
                Arguments.of(visitorDTONullName, Triple.of(null, TestUtils.PHONE_NUM, NOTE)),
                Arguments.of(visitorDTONullPhone, Triple.of(TestUtils.USER_NAME, null, NOTE)),
                Arguments.of(visitorDTONullNote, Triple.of(TestUtils.USER_NAME, TestUtils.PHONE_NUM, null)));
    }
}
