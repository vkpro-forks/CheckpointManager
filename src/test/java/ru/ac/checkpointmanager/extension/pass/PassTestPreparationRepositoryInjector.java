package ru.ac.checkpointmanager.extension.pass;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.repository.VisitorRepository;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.repository.car.CarRepository;

/**
 * Выгружает из контекста Spring репозитории необходимые для тестирования пропусков
 */
@Slf4j
public abstract class PassTestPreparationRepositoryInjector {
    protected ApplicationContext applicationContext;

    protected UserRepository userRepository;
    protected TerritoryRepository territoryRepository;
    protected CarRepository carRepository;
    protected PassRepository passRepository;

    //https://stackoverflow.com/questions/56904620/junit-5-inject-spring-components-to-extension-beforeallcallback-afterallcall
    protected void initRepos(ExtensionContext context) {
        log.info("Set up repositories to save necessary entities");
        applicationContext = SpringExtension.getApplicationContext(context);
        userRepository = applicationContext.getBean(UserRepository.class);
        passRepository = applicationContext.getBean(PassRepository.class);
        territoryRepository = applicationContext.getBean(TerritoryRepository.class);
    }
}
