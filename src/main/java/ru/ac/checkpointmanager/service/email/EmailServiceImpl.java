package ru.ac.checkpointmanager.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
        String htmlContent;
        try {
            ClassPathResource resource = new ClassPathResource("templates/registration-confirmation.html");
            htmlContent = processContent(resource, confirmationRegistrationLink, token);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(prodEmail);
            helper.setTo(to);
            helper.setSubject("Регистрация");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (IOException | MessagingException e) {
            log.error("Error processing email", e);
        }
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
        String htmlContent;
        try {
            ClassPathResource resource = new ClassPathResource("templates/email-confirmation.html");
            htmlContent = processContent(resource, emailChangeConfirmationLink, token);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            helper.setFrom(prodEmail);
            helper.setTo(to);
            helper.setSubject("Подтверждение смены электронной почты");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (IOException | MessagingException e) {
            log.error("Error processing email", e);
        }
    }

    private String processContent(ClassPathResource resource, String link, String token) throws IOException {
        String htmlContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        return htmlContent.replace("${link}", link)
                .replace("${token}", token);
    }
}