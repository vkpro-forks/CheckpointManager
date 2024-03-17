package ru.ac.checkpointmanager.extension;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import ru.ac.checkpointmanager.extension.SavedPassWithRequiredEntitiesDTO;
import ru.ac.checkpointmanager.extension.pass.CarAndVisitorPassTestPreparationRepositoryInjector;

public class PassTestingPreparationExtension extends CarAndVisitorPassTestPreparationRepositoryInjector implements BeforeEachCallback,
        AfterEachCallback, ParameterResolver {

    SavedPassWithRequiredEntitiesDTO savedPassWithRequiredEntitiesDTO = new SavedPassWithRequiredEntitiesDTO();

    @Override
    public void afterEach(ExtensionContext context) {
        passRepository.deleteAll();
        carRepository.deleteAll();
        carBrandRepository.deleteAll();
        territoryRepository.deleteAll();
        userRepository.deleteAll();
        visitorRepository.deleteAll();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {

    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return null;
    }
}
