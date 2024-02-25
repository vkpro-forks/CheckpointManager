package ru.ac.checkpointmanager.assertion;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.InstanceOfAssertFactories;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassWalk;

public class PassAssert extends AbstractAssert<PassAssert, Pass> {
    protected PassAssert(Pass pass) {
        super(pass, PassAssert.class);
    }

    public PassAssert isPassAutoWithMatchedCarFields(String licensePlate, String phone, CarBrand carBrand) {
        isNotNull();
        assertThat(actual).asInstanceOf(InstanceOfAssertFactories.type(PassAuto.class))
                .extracting(PassAuto::getCar).extracting(Car::getLicensePlate, Car::getPhone, Car::getBrand)
                .containsExactly(licensePlate, phone, carBrand);
        return this;
    }

    public PassAssert isPassWalkWithMatchedVisitorFields(String name, String phone, String note) {
        isNotNull();
        assertThat(actual).asInstanceOf(InstanceOfAssertFactories.type(PassWalk.class))
                .extracting(PassWalk::getVisitor).extracting(Visitor::getName, Visitor::getPhone, Visitor::getNote)
                .containsExactly(name, phone, note);
        return this;
    }


    public static PassAssert assertThat(Pass pass) {
        return new PassAssert(pass);
    }

}
