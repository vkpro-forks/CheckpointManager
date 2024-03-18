package ru.ac.checkpointmanager.extension.pass;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.ac.checkpointmanager.repository.PassRepository;
import ru.ac.checkpointmanager.repository.TerritoryRepository;
import ru.ac.checkpointmanager.repository.UserRepository;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;

/**
 * Выгружает из контекста Spring репозитории необходимые для тестирования пропусков
 *
 * @see UserRepository
 * @see TerritoryRepository
 * @see CarBrandRepository
 * @see PassRepository
 */
@Slf4j
public abstract class PassTestPreparationRepositoryInjector {

    protected ApplicationContext applicationContext;

    protected UserRepository userRepository;
    protected TerritoryRepository territoryRepository;
    protected CarBrandRepository carBrandRepository;

    //https://stackoverflow.com/questions/56904620/junit-5-inject-spring-components-to-extension-beforeallcallback-afterallcall
    protected void initRepos(ExtensionContext context) {
        log.info("Set up repositories to save necessary entities");
        this.applicationContext = SpringExtension.getApplicationContext(context);
        this.userRepository = applicationContext.getBean(UserRepository.class);
        this.territoryRepository = applicationContext.getBean(TerritoryRepository.class);
        this.carBrandRepository = applicationContext.getBean(CarBrandRepository.class);
    }
}
