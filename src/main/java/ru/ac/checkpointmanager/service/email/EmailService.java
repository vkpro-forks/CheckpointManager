package ru.ac.checkpointmanager.service.email;

public interface EmailService {

    void send(String to, String token);
}
