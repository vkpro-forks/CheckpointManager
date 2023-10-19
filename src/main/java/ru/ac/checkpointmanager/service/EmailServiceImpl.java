package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableAsync
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;


    @Value("${spring.mail.username}")
    private String email;

    @Override
    @Async
    public void send(String to, String token) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(email);
        message.setTo(to);
        message.setSubject("Registration");
        message.setText("Для подтверждения регистрации, пожалуйста, перейдите по следующей ссылке: "
                + "http://localhost:8080/chpman/authentication/confirm?token=" + token +
                "\nЕсли, это были не вы, игнорируйте это письмо.");

        mailSender.send(message);
    }
}

