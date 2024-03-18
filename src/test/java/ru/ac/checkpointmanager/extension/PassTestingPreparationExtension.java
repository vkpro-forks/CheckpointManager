package ru.ac.checkpointmanager.extension;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import ru.ac.checkpointmanager.extension.annotation.InjectSavedEntitiesForPassTest;
import ru.ac.checkpointmanager.extension.pass.PassTestPreparationRepositoryInjector;
import ru.ac.checkpointmanager.extension.pass.UserTerritoryCarBrandDto;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.List;

/**
 * Расширение для JUnit 5, которое сохраняет в тестовую бд данные для дальнейшего тестирования, после проведения тестов
 * данные удаляются.
 * <p>
 * Сохраненные объекты помещаются в {@link UserTerritoryCarBrandDto}, помеченный аннотацией объект инжектится
 * в тестовый класс
 * </p>
 *
 * @see InjectSavedEntitiesForPassTest
 */
@Slf4j
public class PassTestingPreparationExtension extends PassTestPreparationRepositoryInjector implements BeforeEachCallback,
        AfterEachCallback, ParameterResolver {

    UserTerritoryCarBrandDto userTerritoryCarBrandDto = new UserTerritoryCarBrandDto();

    @Override
    public void afterEach(ExtensionContext context) {
        carBrandRepository.deleteAll();
        territoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        super.initRepos(context);
        log.info("Saving car");
        userTerritoryCarBrandDto.setCarBrand(carBrandRepository.saveAndFlush(TestUtils.getCarBrand()));
        log.info("Saving territory and user");
        Territory territory = new Territory();
        territory.setName(TestUtils.TERR_NAME);
        User user = TestUtils.getUser();
        User savedUser = userRepository.saveAndFlush(user);
        userTerritoryCarBrandDto.setUser(savedUser);
        territory.setUsers(List.of(savedUser));
        userTerritoryCarBrandDto.setTerritory(territoryRepository.saveAndFlush(territory));
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.isAnnotated(InjectSavedEntitiesForPassTest.class)
                && parameterContext.getParameter().getType().equals(UserTerritoryCarBrandDto.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return userTerritoryCarBrandDto;
    }
}
