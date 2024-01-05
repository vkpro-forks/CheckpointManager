package ru.ac.checkpointmanager.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ac.checkpointmanager.dto.passes.PassBaseDTO;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.ext.ValidationContextTestResolver;
import ru.ac.checkpointmanager.util.TestUtils;

import java.time.LocalDateTime;
import java.util.stream.Stream;

@ExtendWith({MockitoExtension.class, ValidationContextTestResolver.class})
class PassTimeValidatorTest {

    PassTimeValidator passTimeValidator = new PassTimeValidator();

    ConstraintValidatorContext constraintContext;

    public PassTimeValidatorTest(ConstraintValidatorContext constraintContext) {
        this.constraintContext = constraintContext;
    }

    @ParameterizedTest
    @MethodSource("getCorrectPassDtoArguments")
    void shouldValidateForCorrectPathDto(PassBaseDTO passDto) {
        boolean valid = passTimeValidator.isValid(passDto, constraintContext);

        Assertions.assertThat(valid).isTrue();
    }

    @ParameterizedTest
    @MethodSource("getIncorrectPassDtoArguments")
    void shouldNotValidateForIncorrectPassDto(PassBaseDTO passDto) {
        boolean valid = passTimeValidator.isValid(passDto, constraintContext);

        Assertions.assertThat(valid).isFalse();
    }

    private static Stream<Object> getCorrectPassDtoArguments() {
        PassUpdateDTO passUpdateDTO = TestUtils.getPassUpdateDTO();
        passUpdateDTO.setStartTime(LocalDateTime.now().plusHours(1));
        passUpdateDTO.setEndTime(LocalDateTime.now().plusHours(2));
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        passCreateDTO.setStartTime(LocalDateTime.now().plusHours(1));
        passCreateDTO.setEndTime(LocalDateTime.now().plusHours(2));
        return Stream.of(
                passUpdateDTO,
                passCreateDTO
        );
    }

    private static Stream<Object> getIncorrectPassDtoArguments() {
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
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
