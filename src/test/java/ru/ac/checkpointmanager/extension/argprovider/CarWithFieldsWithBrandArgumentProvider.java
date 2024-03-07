package ru.ac.checkpointmanager.extension.argprovider;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import ru.ac.checkpointmanager.dto.CarDTO;
import ru.ac.checkpointmanager.util.TestUtils;

import java.util.stream.Stream;

@Slf4j
public class CarWithFieldsWithBrandArgumentProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        log.info("Configure arguments for CarDTO");
        CarDTO carDTO = TestUtils.getCarDto();
        CarDTO carDTOWithNullPlate = new CarDTO(null, null, TestUtils.getCarBrandDTO(), TestUtils.PHONE_NUM);
        CarDTO carDTOWithNullPhone = new CarDTO(null, TestUtils.LICENSE_PLATE, TestUtils.getCarBrandDTO(), null);

        return Stream.of(
                Arguments.of(carDTO, Triple.of(carDTO.getLicensePlate(), carDTO.getBrand(), carDTO.getPhone())),
                Arguments.of(carDTOWithNullPlate, Triple.of(null, TestUtils.getCarBrandDTO(), TestUtils.PHONE_NUM)),
                Arguments.of(carDTOWithNullPhone, Triple.of(TestUtils.LICENSE_PLATE, TestUtils.getCarBrandDTO(), null))
        );
    }
}
