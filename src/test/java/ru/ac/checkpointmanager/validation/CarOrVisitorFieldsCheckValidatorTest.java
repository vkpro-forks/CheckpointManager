package ru.ac.checkpointmanager.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.dto.passes.PassDtoCreate;
import ru.ac.checkpointmanager.util.TestUtils;

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

    @Test
    void shouldValidateWithNotValidForBothFieldsPresent() {
        PassDtoCreate passDtoCreate = TestUtils.getPassDtoCreate();
        passDtoCreate.setCar(new CarDTO());
        passDtoCreate.setVisitor(new VisitorDTO());
        boolean valid = carOrVisitorFieldsValidator.isValid(passDtoCreate, context);
        Assertions.assertThat(valid).isFalse();
    }

    @Test
    void shouldValidateWithNotValidForBothFieldsNull() {
        PassDtoCreate passDtoCreate = TestUtils.getPassDtoCreate();
        passDtoCreate.setCar(new CarDTO());
        passDtoCreate.setVisitor(new VisitorDTO());
        boolean valid = carOrVisitorFieldsValidator.isValid(passDtoCreate, context);
        Assertions.assertThat(valid).isFalse();
    }

    @Test
    void shouldValidateWithValidIfOnlyCarPresent() {
        PassDtoCreate passDtoCreate = TestUtils.getPassDtoCreate();
        passDtoCreate.setVisitor(null);
        passDtoCreate.setCar(new CarDTO());
        boolean valid = carOrVisitorFieldsValidator.isValid(passDtoCreate, context);
        Assertions.assertThat(valid).isTrue();
    }

    @Test
    void shouldValidateWithValidIfOnlyVisitorPresent() {
        PassDtoCreate passDtoCreate = TestUtils.getPassDtoCreate();
        passDtoCreate.setCar(null);
        passDtoCreate.setVisitor(new VisitorDTO());
        boolean valid = carOrVisitorFieldsValidator.isValid(passDtoCreate, context);
        Assertions.assertThat(valid).isTrue();
    }

}
