package ru.ac.checkpointmanager.service;

public interface EmailService {

    void send(String to, String token);
}
