package ru.ac.checkpointmanager.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.dto.passes.PassDtoCreate;
import ru.ac.checkpointmanager.dto.passes.PassDtoUpdate;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.stream.Stream;

class CarOrVisitorFieldsCheckValidatorTest {

    CarOrVisitorFieldsValidator carOrVisitorFieldsValidator;

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
        carOrVisitorFieldsValidator = new CarOrVisitorFieldsValidator();
    }

    @ParameterizedTest
    @MethodSource("getPassDtoWithBotCarAndVisitor")
    void shouldValidateWithNotValidForBothFieldsPresent(Object passDto) {
        boolean valid = carOrVisitorFieldsValidator.isValid(passDto, context);

        Assertions.assertThat(valid).isFalse();
    }

    @ParameterizedTest
    @MethodSource("getPassDtoWithCarAndVisitorNulls")
    void shouldValidateWithNotValidForBothFieldsNull(Object passDto) {
        boolean valid = carOrVisitorFieldsValidator.isValid(passDto, context);

        Assertions.assertThat(valid).isFalse();
    }

    @ParameterizedTest
    @MethodSource("getPassDtoWithOnlyCar")
    void shouldValidateWithValidIfOnlyCarPresent(Object passDto) {
        boolean valid = carOrVisitorFieldsValidator.isValid(passDto, context);

        Assertions.assertThat(valid).isTrue();
    }

    @ParameterizedTest
    @MethodSource("getPassDtoWithOnlyVisitor")
    void shouldValidateWithValidIfOnlyVisitorPresent(Object passDto) {
        boolean valid = carOrVisitorFieldsValidator.isValid(passDto, context);

        Assertions.assertThat(valid).isTrue();
    }

    private static Stream<Object> getPassDtoWithBotCarAndVisitor() {
        PassDtoCreate passDtoCreate = TestUtils.getPassDtoCreate();
        passDtoCreate.setCar(new CarDTO());
        passDtoCreate.setVisitor(new VisitorDTO());
        PassDtoUpdate passDtoUpdate = TestUtils.getPassDtoUpdate();
        passDtoUpdate.setCar(new CarDTO());
        passDtoUpdate.setVisitor(new VisitorDTO());
        return Stream.of(
                passDtoCreate,
                passDtoUpdate
        );
    }

    private static Stream<Object> getPassDtoWithCarAndVisitorNulls() {
        PassDtoCreate passDtoCreate = TestUtils.getPassDtoCreate();
        passDtoCreate.setCar(null);
        passDtoCreate.setVisitor(null);

        PassDtoUpdate passDtoUpdate = TestUtils.getPassDtoUpdate();
        passDtoUpdate.setCar(null);
        passDtoUpdate.setVisitor(null);
        return Stream.of(
                passDtoCreate,
                passDtoUpdate
        );
    }

    private static Stream<Object> getPassDtoWithOnlyCar() {
        PassDtoCreate passDtoCreate = TestUtils.getPassDtoCreate();
        passDtoCreate.setVisitor(null);
        passDtoCreate.setCar(new CarDTO());
        PassDtoUpdate passDtoUpdate = TestUtils.getPassDtoUpdate();
        passDtoUpdate.setVisitor(null);
        passDtoUpdate.setCar(new CarDTO());
        return Stream.of(
                passDtoCreate,
                passDtoUpdate
        );
    }

    private static Stream<Object> getPassDtoWithOnlyVisitor() {
        PassDtoCreate passDtoCreate = TestUtils.getPassDtoCreate();
        passDtoCreate.setVisitor(new VisitorDTO());
        passDtoCreate.setCar(null);
        PassDtoUpdate passDtoUpdate = TestUtils.getPassDtoUpdate();
        passDtoUpdate.setVisitor(new VisitorDTO());
        passDtoUpdate.setCar(null);
        return Stream.of(
                passDtoCreate,
                passDtoUpdate
        );
    }

}
