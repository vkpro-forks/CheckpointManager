package ru.ac.checkpointmanager;

import org.springframework.boot.SpringApplication;
import ru.ac.checkpointmanager.config.CorsTestConfiguration;
import ru.ac.checkpointmanager.config.LocalDevPostgresTestContainersConfiguration;

public class CheckPointManagerLocalDevApplication {

    /**
     * Запускает приложение с временным контейнером Postgres с базовыми настройками
     */
    public static void main(String[] args) {
        SpringApplication.from(CheckpointManagerApplication::main)
                .with(LocalDevPostgresTestContainersConfiguration.class, CorsTestConfiguration.class)
                .run(args);
    }

}
