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
        PassUpdateDTO passUpdateDTO = TestUtils.getPassUpdateDTO();
        passUpdateDTO.setStartTime(LocalDateTime.now().plusHours(1));
        passUpdateDTO.setEndTime(LocalDateTime.now().plusHours(2));
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTO();
        passCreateDTO.setStartTime(LocalDateTime.now().plusHours(1));
        passCreateDTO.setEndTime(LocalDateTime.now().plusHours(2));
        return Stream.of(
                passUpdateDTO,
                passCreateDTO
        );
    }

    private static Stream<Object> getIncorrectPassDtoArguments() {
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTO();
        passCreateDTO.setEndTime(LocalDateTime.now().plusHours(1));
        passCreateDTO.setStartTime(LocalDateTime.now().plusHours(2));
        PassUpdateDTO passUpdateDTO = TestUtils.getPassUpdateDTO();
        passUpdateDTO.setEndTime(LocalDateTime.now().plusHours(1));
        passUpdateDTO.setStartTime(LocalDateTime.now().plusHours(2));
        return Stream.of(
                passUpdateDTO,
                passCreateDTO
        );
    }

}
