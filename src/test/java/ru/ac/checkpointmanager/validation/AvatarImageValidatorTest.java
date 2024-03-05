package ru.ac.checkpointmanager.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.extension.ValidationContextTestResolver;
import ru.ac.checkpointmanager.model.avatar.AvatarProperties;

import java.util.List;
import java.util.stream.Stream;

@ExtendWith({MockitoExtension.class, ValidationContextTestResolver.class})
class AvatarImageValidatorTest {

    AvatarImageValidator avatarImageValidator;

    ConstraintValidatorContext constraintContext;

    public AvatarImageValidatorTest(ConstraintValidatorContext constraintContext) {
        this.constraintContext = constraintContext;
    }

    @BeforeEach
    void init() {
        AvatarProperties avatarProperties = new AvatarProperties();
        avatarProperties.setContentType("image/");
        avatarProperties.setExtensions(List.of("jpg", "jpeg", "png", "ico", "gif"));
        avatarProperties.setMaxSize(DataSize.ofBytes(5));
        avatarImageValidator = new AvatarImageValidator(avatarProperties);
    }

    @ParameterizedTest
    @MethodSource("getIncorrectMultipartFiles")
    void shouldNotValidateNotCorrectMultipartFiles(MultipartFile multipartFile) {
        boolean isValid = avatarImageValidator.isValid(multipartFile, constraintContext);

        Assertions.assertThat(isValid).isFalse();
    }

    @Test
    void shouldValidateGoodMultiPartFile() {
        MockMultipartFile goodFile
                = new MockMultipartFile("avatarFile", "avatar.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2, 3});

        boolean isValid = avatarImageValidator.isValid(goodFile, constraintContext);

        Assertions.assertThat(isValid).isTrue();
    }

    private static Stream<MockMultipartFile> getIncorrectMultipartFiles() {
        MockMultipartFile empty
                = new MockMultipartFile("avatarFile", "avatar.png", MediaType.IMAGE_PNG_VALUE, new byte[]{});
        MockMultipartFile badExtension
                = new MockMultipartFile("avatarFile", "avatar.buba", MediaType.IMAGE_PNG_VALUE, new byte[]{1, 2});
        MockMultipartFile wrongContentType
                = new MockMultipartFile("avatarFile", "avatar.png", MediaType.APPLICATION_JSON_VALUE, new byte[]{1, 2});
        MockMultipartFile tooBig
                = new MockMultipartFile("avatarFile", "avatar.png", MediaType.IMAGE_PNG_VALUE,
                new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        return Stream.of(
                empty, badExtension, wrongContentType, tooBig
        );
    }

}