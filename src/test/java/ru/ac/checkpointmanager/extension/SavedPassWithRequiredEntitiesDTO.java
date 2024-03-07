package ru.ac.checkpointmanager.extension;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.passes.Pass;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedPassWithRequiredEntitiesDTO {

    private Territory territory;

    private User user;

    private CarBrand carBrand;

    private Car car;

    private Pass pass;
}
