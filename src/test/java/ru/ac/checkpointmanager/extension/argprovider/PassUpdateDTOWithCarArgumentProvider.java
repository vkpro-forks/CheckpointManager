package ru.ac.checkpointmanager.extension.argprovider;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import ru.ac.checkpointmanager.dto.CarBrandDTO;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.model.passes.PassTimeType;
import ru.ac.checkpointmanager.util.TestUtils;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
public class PassUpdateDTOWithCarArgumentProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        log.info("Configuring passUpdateDtos with Car for checking how pass will be updated");
        String anotherLicensePlate = "А425ВХ799";
        String updatedComment = "my comment";
        String newBrand = "Java4Ever";
        PassUpdateDTO passDTOForSavedCar = new PassUpdateDTO(updatedComment, PassTimeType.PERMANENT,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
                null, new CarDTO(TestUtils.getCarDto().getId(), anotherLicensePlate,
                new CarBrandDTO(TestUtils.getCarBrand().getBrand()), null), UUID.randomUUID());
        PassUpdateDTO passDTOForNotSavedCar = new PassUpdateDTO(updatedComment, PassTimeType.PERMANENT,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
                null, new CarDTO(null, anotherLicensePlate,
                new CarBrandDTO(TestUtils.getCarBrand().getBrand()), null), UUID.randomUUID());
        PassUpdateDTO passDTOForNotSavedAnotherType = new PassUpdateDTO(updatedComment, PassTimeType.ONETIME,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
                null, new CarDTO(null, anotherLicensePlate,
                new CarBrandDTO(TestUtils.getCarBrand().getBrand()), null), UUID.randomUUID());

        PassUpdateDTO passDTOForNotSavedAnotherTypeNewBrand = new PassUpdateDTO(updatedComment, PassTimeType.ONETIME,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
                null, new CarDTO(null, anotherLicensePlate,
                new CarBrandDTO(newBrand), null), UUID.randomUUID());

        return Stream.of(
                Arguments.of(passDTOForSavedCar),
                Arguments.of(passDTOForNotSavedCar),
                Arguments.of(passDTOForNotSavedAnotherType),
                Arguments.of(passDTOForNotSavedAnotherTypeNewBrand)
        );
    }
}
