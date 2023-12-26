package ru.ac.checkpointmanager;

import org.springframework.boot.SpringApplication;
import ru.ac.checkpointmanager.config.CorsTestConfiguration;
import ru.ac.checkpointmanager.config.LocalDevPostgresTestContainersConfiguration;
import ru.ac.checkpointmanager.config.LocalDevRedisTestContainersConfiguration;

public class CheckPointManagerLocalDevApplication {

    /**
     * Запускает приложение с временным контейнером Postgres с базовыми настройками
     * Важно запускать c environment: spring.profiles.active=test, чтобы взять все настройки из тестового конфига
     * (В обычном конфиге в самом начале логбэк пытается достучаться до бд, которая указана в пропертях,
     * а не до той, которая поднимается в контейнере
     * @param args аргументы
     */
    public static void main(String[] args) {
        SpringApplication.from(CheckpointManagerApplication::main)
                .with(LocalDevPostgresTestContainersConfiguration.class, CorsTestConfiguration.class,
                        LocalDevRedisTestContainersConfiguration.class)
                .run(args);
    }

}
