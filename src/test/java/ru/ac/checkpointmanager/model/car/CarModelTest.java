package ru.ac.checkpointmanager.model.car;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.ac.checkpointmanager.model.car.CarModel;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CarModelTest {

    private CarModel carModel;

    @BeforeEach
    public void setUp() {
        carModel = new CarModel();
    }

    @Test
    public void toProperName_withAllLowerCase_convertsToProperName() {
        carModel.setModel("camry");

        carModel.toProperName();

        assertEquals("Camry", carModel.getModel());
    }

    @Test
    public void toProperName_withAllUpperCase_convertsToProperName() {
        carModel.setModel("CAMRY");

        carModel.toProperName();

        assertEquals("Camry", carModel.getModel());
    }

    @Test
    public void toProperName_withMixedCase_convertsToProperName() {
        carModel.setModel("cAmRY");

        carModel.toProperName();

        assertEquals("Camry", carModel.getModel());
    }

    @Test
    public void toProperName_withNull_doesNotThrowException() {
        carModel.setModel(null);

        carModel.toProperName();

        assertEquals(null, carModel.getModel());
    }
}
