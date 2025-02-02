package ru.ac.checkpointmanager.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import ru.ac.checkpointmanager.service.email.EmailServiceImpl;

import java.util.Properties;

/**
 * Конфигурация для настройки отправки электронной почты из приложения.
 * <p>
 * Этот класс используется для настройки {@link JavaMailSender}, который отвечает за отправку электронных писем.
 * Он определяет свойства, такие как адрес электронной почты отправителя, пароль, хост и порт сервера электронной почты.
 *
 * @author fifimova
 */
@Configuration
public class EmailConfig {

    @Value("${spring.mail.username}")
    private String prodEmail;

    @Value("${spring.mail.password}")
    private String password;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.protocol}")
    private String protocol;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    private String auth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private String starttls;

    @Value("${spring.mail.properties.mail.smtp.ssl.enable}")
    private String ssl;

    /**
     * Возвращает настроенный экземпляр {@link JavaMailSender} для отправки электронной почты.
     * <p>
     * Этот метод создает и настраивает {@link JavaMailSenderImpl}, который используется для отправки электронных писем.
     * Он устанавливает хост и порт сервера электронной почты, а также устанавливает имя пользователя и пароль для аутентификации.
     * <p>
     * Метод также устанавливает дополнительные свойства {@code JavaMailSenderImpl}, такие, как: протокол, аутентификация, включение TLS и SSL.
     *
     * @return настроенный экземпляр JavaMailSender для отправки электронной почты
     * @see EmailServiceImpl
     */
    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        mailSender.setUsername(prodEmail);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.protocol", protocol);
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", starttls);
        props.put("mail.smtp.ssl.enable", ssl);

        return mailSender;
    }
}
