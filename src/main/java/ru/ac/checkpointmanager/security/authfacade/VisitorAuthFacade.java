package ru.ac.checkpointmanager.security.authfacade;

import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.repository.VisitorRepository;

import java.util.UUID;

@Component("visitorAuthFacade")
public final class VisitorAuthFacade implements AuthFacade {

    private final VisitorRepository visitorRepository;

    public VisitorAuthFacade(VisitorRepository visitorRepository) {
        this.visitorRepository = visitorRepository;
    }

    /**
     * Проверка, имеет ли пользователь доступ к посетителю
     * на основании наличия пропуска с посетителем по переданному Id
     *
     * @param visitorId UUID, который необходимо проверить на совпадение.
     * @return true - если пользователь и посетитель связаны пропуском, false - если нет
     */
    @Override
    public boolean isIdMatch(UUID visitorId) {
        UUID userId = getCurrentUser().getId();
        return visitorRepository.checkUserVisitorRelation(userId, visitorId);
    }
}
