package ru.ac.checkpointmanager.extension.pass;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.extension.annotation.InjectSavedEntitiesForPassTest;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.car.CarBrand;

/**
 * DTO с набором данных сохраненных в репозитории объектов для использования в тестах
 * @see ru.ac.checkpointmanager.extension.PassTestingPreparationExtension
 * @see InjectSavedEntitiesForPassTest
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserTerritoryCarBrandDto {

    private Territory territory;

    private User user;

    private CarBrand carBrand;
}
