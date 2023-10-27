package ru.ac.checkpointmanager;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class Main {
    public static void main(String[] args) {
        // тестовый класс для проверки

// ...

        System.out.println(ZonedDateTime.now(ZoneId.systemDefault()));


        //проверка часового пояса
        TimeZone tz = TimeZone.getDefault();
        System.out.println("Current TimeZone is : " + tz.getID());

    }
}
