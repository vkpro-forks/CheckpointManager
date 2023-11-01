package ru.ac.checkpointmanager.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.model.TemporaryUser;
import ru.ac.checkpointmanager.repository.TemporaryUserRepository;

import java.time.LocalDateTime;

/**
 * Реализация сервиса для работы с временными пользователями.
 *
 * @author fifimova
 * @see TemporaryUser
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TemporaryUserServiceImpl implements TemporaryUserService {

    private final TemporaryUserRepository repository;

    private int hourForLogInScheduledCheck;

    @Override
    public TemporaryUser findByVerifiedToken(String verifiedToken) {
        return repository.findByVerifiedToken(verifiedToken);
    }

    @Override
    public TemporaryUser create(TemporaryUser temporaryUser) {
        return repository.save(temporaryUser);
    }

    @Override
    public void delete(TemporaryUser temporaryUser) {
        repository.delete(temporaryUser);
    }

    /**
     * Этот метод запускается каждые 30 секунд и удаляет все записи в таблице временных пользователей, которые были созданы более 15 минут назад.
     * После удаления пользователя из временной таблицы, ссылка для подтверждения регистрации станет недействительной.
     * Метод также записывает информацию в журнал о своем выполнении каждый час.
     *
     * @see TemporaryUser
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void cleanup() {
        LocalDateTime now = LocalDateTime.now();
        if (now.getHour() != hourForLogInScheduledCheck) {
            hourForLogInScheduledCheck = now.getHour();
            log.debug("Scheduled method 'cleanup' for temporary_users table continues to work");
        }
        LocalDateTime cutoffTime = now.minusHours(1);
        repository.deleteByAddedAtBefore(cutoffTime);
    }
}