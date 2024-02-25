package ru.ac.checkpointmanager.util;

import lombok.experimental.UtilityClass;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.assertj.core.api.ThrowableAssertAlternative;

@UtilityClass
public class AssertionsUtils {

    public static ThrowableAssertAlternative<? extends Throwable> checkExceptionTypeWithMessage(
            ThrowableAssert.ThrowingCallable throwingCallable,
            Class<? extends Throwable> exception, String message) {
        return Assertions.assertThatExceptionOfType(exception)
                .isThrownBy(throwingCallable)
                .withMessage(message);
    }

    public static ThrowableAssertAlternative<? extends Throwable> checkExceptionTypeWithParentAndMessage(
            ThrowableAssert.ThrowingCallable throwingCallable,
            Class<? extends Throwable> exception,
            Class<? extends Throwable> parent, String message) {
        return checkExceptionTypeWithMessage(throwingCallable, exception, message).isInstanceOf(parent);
    }
}
