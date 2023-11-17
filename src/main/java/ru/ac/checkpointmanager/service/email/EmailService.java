package ru.ac.checkpointmanager.service.email;

public interface EmailService {

    void sendRegisterConfirm(String to, String token);

    void sendEmailConfirm(String to, String token);
}