package ru.ac.checkpointmanager.utils;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public class FieldsValidation {

    public static String cleanPhone(String phone) {
        // Удаление всех символов, кроме цифр
        String cleanedPhone = phone.replaceAll("[^\\d]", "");

        // Удаление ведущих нулей
        cleanedPhone = cleanedPhone.replaceFirst("^0+(?!$)", "");

        return cleanedPhone;
    }

    @Deprecated
    public static Boolean validateDOB(LocalDate dateOfBirth) {
        LocalDate currentDate = LocalDate.now();
        if (dateOfBirth.isAfter(currentDate)) {
            return false; // Date of birth is in the future
        }

        Period age = Period.between(dateOfBirth, currentDate);
        return age.getYears() <= 100; // Date of birth is at least 100 years ago
    }
}
