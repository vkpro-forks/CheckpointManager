package ru.ac.checkpointmanager.assertion;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.InstanceOfAssertFactories;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassTimeType;
import ru.ac.checkpointmanager.model.passes.PassWalk;

import java.time.LocalDateTime;

public class AssertPass extends AbstractAssert<AssertPass, Pass> {
    protected AssertPass(Pass pass) {
        super(pass, AssertPass.class);
    }

    public AssertPass isPassAutoWithMatchedCarFields(String licensePlate, String phone, CarBrand carBrand) {
        isNotNull();
        assertThat(actual).asInstanceOf(InstanceOfAssertFactories.type(PassAuto.class))
                .extracting(PassAuto::getCar).extracting(Car::getLicensePlate, Car::getPhone, Car::getBrand)
                .containsExactly(licensePlate, phone, carBrand);
        return this;
    }

    public AssertPass isPassWalkWithMatchedVisitorFields(String name, String phone, String note) {
        isNotNull();
        assertThat(actual).asInstanceOf(InstanceOfAssertFactories.type(PassWalk.class))
                .extracting(PassWalk::getVisitor).extracting(Visitor::getName, Visitor::getPhone, Visitor::getNote)
                .containsExactly(name, phone, note);
        return this;
    }

    public AssertPass isPassFieldsMatches(String comment, LocalDateTime startTime, LocalDateTime endTime,
                                          PassTimeType passTimeType) {
        isNotNull();
        assertThat(actual).asInstanceOf(InstanceOfAssertFactories.type(Pass.class))
                .extracting(Pass::getComment, Pass::getStartTime, Pass::getEndTime, Pass::getTimeType)
                .containsExactly(comment, startTime, endTime, passTimeType);
        return this;
    }

    public static AssertPass assertThat(Pass pass) {
        return new AssertPass(pass);
    }

}
