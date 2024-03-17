package ru.ac.checkpointmanager.extension.pass;

import org.junit.jupiter.api.extension.ExtensionContext;
import ru.ac.checkpointmanager.repository.car.CarBrandRepository;
import ru.ac.checkpointmanager.repository.car.CarRepository;

public abstract class CarPassTestPreparationRepositoryInjector extends PassTestPreparationRepositoryInjector {

    protected CarRepository carRepository;
    protected CarBrandRepository carBrandRepository;

    @Override
    protected void initRepos(ExtensionContext context) {
        super.initRepos(context);
        carBrandRepository = applicationContext.getBean(CarBrandRepository.class);
        carRepository = applicationContext.getBean(CarRepository.class);
    }
}
