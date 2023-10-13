package ru.ac.checkpointmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class CheckpointManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheckpointManagerApplication.class, args);
    }
}
