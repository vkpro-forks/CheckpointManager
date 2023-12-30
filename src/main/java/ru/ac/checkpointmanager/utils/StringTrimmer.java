package ru.ac.checkpointmanager.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@Slf4j
public final class StringTrimmer {

    private StringTrimmer() {
        throw new AssertionError("No StringTrimmer instances for you!");
    }

    /**
     * во всех полях типа String принятого объекта удаляет двойные пробелы,
     * а также пробелы в начале и конце строки
     * @param object экземпляр любой из сущностей
     */
    public static void trimThemAll(Object object) {
        List<Field> fields = Arrays.stream(object.getClass().getDeclaredFields()).toList();
        fields.forEach(field -> {
            field.setAccessible(true);
            if (field.getType() == String.class) {
                try {
                    String value = (String) field.get(object);
                    if (value != null) {
                        field.set(object, value
                                .replaceAll("\\s+", " ")
                                .trim());
                    }
                } catch (IllegalAccessException e) {
                    log.warn("Something went wrong while trimming string fields in the object: " + e.getMessage());
                }
            }
        });
    }
}