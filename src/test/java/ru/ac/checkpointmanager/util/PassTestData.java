package ru.ac.checkpointmanager.util;

import lombok.experimental.UtilityClass;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.model.passes.PassTimeType;
import ru.ac.checkpointmanager.model.passes.PassWalk;
import ru.ac.checkpointmanager.specification.model.PassAuto_;
import ru.ac.checkpointmanager.specification.model.PassWalk_;

import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class PassTestData {

    public static final UUID PASS_ID = UUID.randomUUID();

    public static PassUpdateDTO getPassUpdateDTOWithCar() {
        return new PassUpdateDTO(
                "comment",
                PassTimeType.ONETIME,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(7),
                null,
                TestUtils.getCarDto(),
                PASS_ID
        );
    }

    public static PassUpdateDTO getPassUpdateDTOVisitor() {
        return new PassUpdateDTO(
                "comment",
                PassTimeType.ONETIME,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(7),
                TestUtils.getVisitorDTO(),
                null,
                PASS_ID
        );
    }

    public static PassCreateDTO getPassCreateDTOWithCar() {
        return new PassCreateDTO(
                "comment",
                PassTimeType.ONETIME,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(7),
                null,
                TestUtils.getCarDto(),
                TestUtils.USER_ID,
                TestUtils.TERR_ID
        );
    }

    public static PassCreateDTO getPassCreateDTOWithVisitor() {
        return new PassCreateDTO(
                "comment",
                PassTimeType.ONETIME,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(7),
                TestUtils.getVisitorDTO(),
                null,
                TestUtils.USER_ID,
                TestUtils.TERR_ID
        );
    }

    public static PassAuto getSimpleActiveOneTimePassAutoFor3Hours(User user, Territory territory, Car car) {
        PassAuto passAuto = new PassAuto();
        passAuto.setStartTime(LocalDateTime.now());
        passAuto.setEndTime(LocalDateTime.now().plusHours(3));
        passAuto.setId(UUID.randomUUID());
        passAuto.setTimeType(PassTimeType.ONETIME);
        passAuto.setDtype(PassAuto_.DTYPE);
        passAuto.setStatus(PassStatus.ACTIVE);
        passAuto.setCar(car);
        passAuto.setUser(user);
        passAuto.setTerritory(territory);
        passAuto.setId(UUID.randomUUID());
        return passAuto;
    }

    public static PassAuto getSimpleActivePermanentAutoFor3Hours(User user, Territory territory, Car car) {
        PassAuto passAuto = new PassAuto();
        passAuto.setStartTime(LocalDateTime.now());
        passAuto.setEndTime(LocalDateTime.now().plusHours(3));
        passAuto.setId(UUID.randomUUID());
        passAuto.setTimeType(PassTimeType.PERMANENT);
        passAuto.setDtype(PassAuto_.DTYPE);
        passAuto.setStatus(PassStatus.ACTIVE);
        passAuto.setCar(car);
        passAuto.setUser(user);
        passAuto.setTerritory(territory);
        passAuto.setId(UUID.randomUUID());
        return passAuto;
    }

    public static PassWalk getSimpleActiveOneTimePassWalkFor3Hours(User user, Territory territory, Visitor visitor) {
        PassWalk passWalk = new PassWalk();
        passWalk.setStartTime(LocalDateTime.now());
        passWalk.setEndTime(LocalDateTime.now().plusHours(3));
        passWalk.setId(UUID.randomUUID());
        passWalk.setTimeType(PassTimeType.ONETIME);
        passWalk.setDtype(PassWalk_.DTYPE);
        passWalk.setStatus(PassStatus.ACTIVE);
        passWalk.setVisitor(visitor);
        passWalk.setUser(user);
        passWalk.setTerritory(territory);
        passWalk.setId(UUID.randomUUID());
        return passWalk;
    }

    public static PassWalk getPassWalk(PassStatus passStatus, LocalDateTime startTime, LocalDateTime endTime, User savedUser,
                                       Territory savedTerritory, Visitor savedVisitor, PassTimeType passTimeType) {
        PassWalk passWalk = new PassWalk();
        passWalk.setStatus(passStatus);
        passWalk.setStartTime(startTime);
        passWalk.setEndTime(endTime);
        passWalk.setUser(savedUser);
        passWalk.setDtype(PassWalk_.DTYPE);
        passWalk.setTerritory(savedTerritory);
        passWalk.setVisitor(savedVisitor);//name USERNAME
        passWalk.setTimeType(passTimeType);
        passWalk.setId(UUID.randomUUID());
        return passWalk;
    }
}
