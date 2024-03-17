package ru.ac.checkpointmanager.extension.pass;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarVisitorUserTerritoryDto {

    private Territory territory;

    private User user;

    private CarBrand carBrand;
}
