package ru.ac.checkpointmanager.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.utils.MethodLog;

/**
 * Сервис для отправки электронных писем.
 * <p>
 * Этот класс предоставляет функциональность для отправки электронных писем с помощью {@link JavaMailSender}.
 * Класс использует параметры из конфигурации приложения для определения отправителя письма и ссылки для подтверждения регистрации.
 *
 * @author fifimova
 * @see ru.ac.checkpointmanager.configuration.EmailConfig
 */
@Service
@RequiredArgsConstructor
@Slf4j
@EnableAsync
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String prodEmail;

    @Value("${app.confirmation-link}")
    private String confirmationRegistrationLink;

    @Value("${app.confirmation-email-link}")
    private String emailChangeConfirmationLink;

    /**
     * Отправляет электронное письмо со ссылкой для подтверждения регистрации.
     * <p>
     * Метод использует асинхронную отправку писем на указанный адрес со ссылкой для подтверждения регистрации.
     * Ссылка для подтверждения формируется путем добавления предоставленного токена к базовой ссылке, указанной
     * в конфигурации приложения.
     *
     * @param to    адрес электронной почты, на который будет отправлено письмо
     * @param token токен, который будет добавлен к ссылке для подтверждения регистрации
     * @see ru.ac.checkpointmanager.configuration.EmailConfig
     */
    @Override
    @Async
    public void sendRegisterConfirm(String to, String token) {
        log.debug("Method {}", MethodLog.getMethodName());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(prodEmail);
        message.setTo(to);
        message.setSubject("Registration");
        message.setText("Для подтверждения регистрации, пожалуйста, перейдите по следующей ссылке: "
                + confirmationRegistrationLink + token +
                "\nЕсли, это были не вы, игнорируйте это письмо.");

        mailSender.send(message);
    }

    /**
     * Отправляет электронное письмо со ссылкой для подтверждения новой электронной почты.
     * <p>
     * Метод использует асинхронную отправку писем на указанный адрес со ссылкой для подтверждения почты.
     * Ссылка для подтверждения формируется путем добавления предоставленного токена к базовой ссылке, указанной
     * в конфигурации приложения.
     *
     * @param to    адрес электронной почты, на который будет отправлено письмо
     * @param token токен, который будет добавлен к ссылке для подтверждения новой почты
     * @see ru.ac.checkpointmanager.configuration.EmailConfig
     */
    @Override
    @Async
    public void sendEmailConfirm(String to, String token) {
        log.info("Method sendEmailConfirm was invoked");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(prodEmail);
        message.setTo(to);
        message.setSubject("Email confirmation");
        message.setText("Чтобы подтвердить новый адрес электронной почты, перейдите по ссылке: "
                + emailChangeConfirmationLink + token +
                "\nЕсли, это были не вы, игнорируйте это письмо.");
        mailSender.send(message);
    }
}