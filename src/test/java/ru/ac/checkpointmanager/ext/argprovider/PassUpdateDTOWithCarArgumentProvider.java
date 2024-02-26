package ru.ac.checkpointmanager.ext.argprovider;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import ru.ac.checkpointmanager.dto.CarBrandDTO;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.passes.PassTimeType;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
public class PassUpdateDTOWithCarArgumentProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        log.info("Configuring passUpdateDtos for checking how pass will be updated");
        Car savedCar = context.getStore(ExtensionContext.Namespace.GLOBAL).get("savedCar", Car.class);
        String anotherLicensePlate = "А425ВХ799";
        String updatedComment = "my comment";

        PassUpdateDTO passUpdateDTOWithSavedCar = new PassUpdateDTO(updatedComment, PassTimeType.PERMANENT,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
                null, new CarDTO(savedCar.getId(), anotherLicensePlate,
                new CarBrandDTO(savedCar.getBrand().getBrand()), null), UUID.randomUUID());
        return Stream.of(
                Arguments.of(passUpdateDTOWithSavedCar)
        );
    }
}
