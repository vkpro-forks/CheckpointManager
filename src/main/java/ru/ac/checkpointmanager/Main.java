package ru.ac.checkpointmanager;

import java.util.TimeZone;

public class Main {
    public static void main(String[] args) {
        // тестовый класс для проверки



        //проверка часового пояса
        TimeZone tz = TimeZone.getDefault();
        System.out.println("Current TimeZone is : " + tz.getID());

    }
}
