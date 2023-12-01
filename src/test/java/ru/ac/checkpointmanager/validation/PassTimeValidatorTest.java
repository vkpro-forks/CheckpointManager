package ru.ac.checkpointmanager.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.util.TestUtils;

import java.time.LocalDateTime;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class PassTimeValidatorTest {

    PassTimeValidator passTimeValidator;

    ConstraintValidatorContext context;

    @BeforeEach
    void init() {
        context = Mockito.mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder cvBuilder = Mockito
                .mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        Mockito.when(context.buildConstraintViolationWithTemplate(Mockito.any())).thenReturn(cvBuilder);
        ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext
                nodeBuilderCustomizableContext = Mockito
                .mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);
        Mockito.when(nodeBuilderCustomizableContext.addConstraintViolation()).thenReturn(context);
        Mockito.when(cvBuilder.addPropertyNode(Mockito.any())).thenReturn(nodeBuilderCustomizableContext);
        passTimeValidator = new PassTimeValidator();
    }

    @ParameterizedTest
    @MethodSource("getCorrectPassDtoArguments")
    void shouldValidateForCorrectPathDto(Object passDto) {
        boolean valid = passTimeValidator.isValid(passDto, context);

        Assertions.assertThat(valid).isTrue();
    }

    @ParameterizedTest
    @MethodSource("getIncorrectPassDtoArguments")
    void shouldNotValidateForIncorrectPassDto(Object passDto) {
        boolean valid = passTimeValidator.isValid(passDto, context);

        Assertions.assertThat(valid).isFalse();
    }

    private static Stream<Object> getCorrectPassDtoArguments() {
        PassUpdateDTO passDtoUpdate = TestUtils.getPassUpdateDTO();
        passDtoUpdate.setStartTime(LocalDateTime.now().plusHours(1));
        passDtoUpdate.setEndTime(LocalDateTime.now().plusHours(2));
        PassCreateDTO passDtoCreate = TestUtils.getPassCreateDTO();
        passDtoCreate.setStartTime(LocalDateTime.now().plusHours(1));
        passDtoCreate.setEndTime(LocalDateTime.now().plusHours(2));
        return Stream.of(
                passDtoUpdate,
                passDtoCreate
        );
    }

    private static Stream<Object> getIncorrectPassDtoArguments() {
        PassCreateDTO passDtoCreate = TestUtils.getPassCreateDTO();
        passDtoCreate.setEndTime(LocalDateTime.now().plusHours(1));
        passDtoCreate.setStartTime(LocalDateTime.now().plusHours(2));
        PassUpdateDTO passDtoUpdate = TestUtils.getPassUpdateDTO();
        passDtoUpdate.setEndTime(LocalDateTime.now().plusHours(1));
        passDtoUpdate.setStartTime(LocalDateTime.now().plusHours(2));
        return Stream.of(
                passDtoUpdate,
                passDtoCreate
        );
    }

}
