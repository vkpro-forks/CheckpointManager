package ru.ac.checkpointmanager.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public final class StringTrimmer {
    /**
     * во всех полях типа String принятого объекта удаляет двойные пробелы,
     * а также пробелы в начале и конце строки
     * @param object экземпляр любой из сущностей
     */
    public static void trimThemAll(Object object) {
        List<Field> fields = Arrays.stream(object.getClass().getDeclaredFields()).toList();
        fields.forEach(field -> {
            // Сделать приватные поля доступными
            field.setAccessible(true);
            if (field.getType() == String.class) {
                try {
                    // Получить значение поля из экземпляра объекта
                    String value = (String) field.get(object);
                    // Удалить лишние пробелы и обновить поле
                    if (value != null) {
                        field.set(object, value
                                .replaceAll("\\s+", " ")
                                .trim());
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}