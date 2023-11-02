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

    public static boolean isValidPhoneNumber(String phoneNumber) {
        String regex = "^([+]?[\\s0-9]+)?(\\d{3}|[(]?[0-9]+[)])?([-]?[\\s]?[0-9])+$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(phoneNumber).matches();
    }

    public static Boolean validateDOB(LocalDate dateOfBirth) {
        LocalDate currentDate = LocalDate.now();
        if (dateOfBirth.isAfter(currentDate)) {
            return false; // Date of birth is in the future
        }

        Period age = Period.between(dateOfBirth, currentDate);
        return age.getYears() <= 100; // Date of birth is at least 100 years ago
    }
}
