package ru.ac.checkpointmanager.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public final class StringTrimmer {
    public static void trimThemAll(Object object) {
        List<Field> fields = Arrays.stream(object.getClass().getDeclaredFields()).toList();
        fields.forEach(field -> {
            field.setAccessible(true); // Установите флаг доступности для приватных полей
            if (field.getType() == String.class) { // Проверка типа поля
                try {
                    String value = (String) field.get(object); // Получение значения поля из экземпляра объекта
                    // Если значение не равно null, преобразуйте его к верхнему регистру и обновите поле
                    if (value != null) {
                        field.set(object, value
                                //удаляет двойные пробелы
                                .replaceAll("\\s+", " ")
                                //удаляет пробелы в начале и в конце строки
                                .trim());
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}