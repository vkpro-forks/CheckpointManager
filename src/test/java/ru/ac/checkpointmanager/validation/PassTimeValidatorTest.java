package ru.ac.checkpointmanager.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ac.checkpointmanager.dto.passes.PassDtoCreate;
import ru.ac.checkpointmanager.dto.passes.PassDtoUpdate;
import ru.ac.checkpointmanager.util.TestUtils;

import java.time.LocalDateTime;

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

    @Test
    void shouldNotValidateForPassDtoCreate() {
        PassDtoCreate passDtoCreate = TestUtils.getPassDtoCreate();
        passDtoCreate.setEndTime(LocalDateTime.now().plusHours(1));
        passDtoCreate.setStartTime(LocalDateTime.now().plusHours(2));

        boolean valid = passTimeValidator.isValid(passDtoCreate, context);

        Assertions.assertThat(valid).isFalse();
    }

    @Test
    void shouldValidateForPassDtoCreate() {
        PassDtoCreate passDtoCreate = TestUtils.getPassDtoCreate();
        passDtoCreate.setStartTime(LocalDateTime.now().plusHours(1));
        passDtoCreate.setEndTime(LocalDateTime.now().plusHours(2));

        boolean valid = passTimeValidator.isValid(passDtoCreate, context);

        Assertions.assertThat(valid).isTrue();
    }

    @Test
    void shouldNotValidateForPassDtoUpdate() {
        PassDtoUpdate passDtoUpdate = TestUtils.getPassDtoUpdate();
        passDtoUpdate.setEndTime(LocalDateTime.now().plusHours(1));
        passDtoUpdate.setStartTime(LocalDateTime.now().plusHours(2));

        boolean valid = passTimeValidator.isValid(passDtoUpdate, context);

        Assertions.assertThat(valid).isFalse();
    }

    @Test
    void shouldValidateForPassDtoUpdate() {
        PassDtoUpdate passDtoUpdate = TestUtils.getPassDtoUpdate();
        passDtoUpdate.setStartTime(LocalDateTime.now().plusHours(1));
        passDtoUpdate.setEndTime(LocalDateTime.now().plusHours(2));

        boolean valid = passTimeValidator.isValid(passDtoUpdate, context);

        Assertions.assertThat(valid).isTrue();
    }

}