package ru.ac.checkpointmanager.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.dto.passes.PassBaseDTO;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.ext.ValidationContextTestResolver;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.stream.Stream;

@ExtendWith({MockitoExtension.class, ValidationContextTestResolver.class})
class CarOrVisitorFieldsCheckValidatorTest {

    CarOrVisitorFieldsValidator carOrVisitorFieldsValidator = new CarOrVisitorFieldsValidator();

    ConstraintValidatorContext constraintContext;

    public CarOrVisitorFieldsCheckValidatorTest(ConstraintValidatorContext constraintContext) {
        this.constraintContext = constraintContext;
    }

    @ParameterizedTest
    @MethodSource("getPassDtoWithBotCarAndVisitor")
    void shouldValidateWithNotValidForBothFieldsPresent(PassBaseDTO passDto) {
        boolean valid = carOrVisitorFieldsValidator.isValid(passDto, constraintContext);

        Assertions.assertThat(valid).isFalse();
    }

    @ParameterizedTest
    @MethodSource("getPassDtoWithCarAndVisitorNulls")
    void shouldValidateWithNotValidForBothFieldsNull(PassBaseDTO passDto) {
        boolean valid = carOrVisitorFieldsValidator.isValid(passDto, constraintContext);

        Assertions.assertThat(valid).isFalse();
    }

    @ParameterizedTest
    @MethodSource("getPassDtoWithOnlyCar")
    void shouldValidateWithValidIfOnlyCarPresent(PassBaseDTO passDto) {
        boolean valid = carOrVisitorFieldsValidator.isValid(passDto, constraintContext);

        Assertions.assertThat(valid).isTrue();
    }

    @ParameterizedTest
    @MethodSource("getPassDtoWithOnlyVisitor")
    void shouldValidateWithValidIfOnlyVisitorPresent(PassBaseDTO passDto) {
        boolean valid = carOrVisitorFieldsValidator.isValid(passDto, constraintContext);

        Assertions.assertThat(valid).isTrue();
    }

    private static Stream<Object> getPassDtoWithBotCarAndVisitor() {
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        passCreateDTO.setCar(new CarDTO());
        passCreateDTO.setVisitor(new VisitorDTO());
        PassUpdateDTO passUpdateDTO = TestUtils.getPassUpdateDTOWithCar();
        passUpdateDTO.setCar(new CarDTO());
        passUpdateDTO.setVisitor(new VisitorDTO());
        return Stream.of(
                passCreateDTO,
                passUpdateDTO
        );
    }

    private static Stream<Object> getPassDtoWithCarAndVisitorNulls() {
        PassCreateDTO passDtoCreate = TestUtils.getPassCreateDTOWithCar();
        passDtoCreate.setCar(null);
        passDtoCreate.setVisitor(null);

        PassUpdateDTO passDtoUpdate = TestUtils.getPassUpdateDTOWithCar();
        passDtoUpdate.setCar(null);
        passDtoUpdate.setVisitor(null);
        return Stream.of(
                passDtoCreate,
                passDtoUpdate
        );
    }

    private static Stream<Object> getPassDtoWithOnlyCar() {
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        passCreateDTO.setVisitor(null);
        passCreateDTO.setCar(new CarDTO());
        PassUpdateDTO passUpdateDTO = TestUtils.getPassUpdateDTOWithCar();
        passUpdateDTO.setVisitor(null);
        passUpdateDTO.setCar(new CarDTO());
        return Stream.of(
                passCreateDTO,
                passUpdateDTO
        );
    }

    private static Stream<Object> getPassDtoWithOnlyVisitor() {
        PassCreateDTO passCreateDTO = TestUtils.getPassCreateDTOWithCar();
        passCreateDTO.setVisitor(new VisitorDTO());
        passCreateDTO.setCar(null);
        PassUpdateDTO passUpdateDTO = TestUtils.getPassUpdateDTOWithCar();
        passUpdateDTO.setVisitor(new VisitorDTO());
        passUpdateDTO.setCar(null);
        return Stream.of(
                passCreateDTO,
                passUpdateDTO
        );
    }

}
