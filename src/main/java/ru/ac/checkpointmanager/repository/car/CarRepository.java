package ru.ac.checkpointmanager.repository.car;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ac.checkpointmanager.model.car.Car;

import java.util.List;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {

    @Query(value = "SELECT c.* FROM cars c JOIN passes p ON c.id = p.car_id" +
            " WHERE p.user_id = :userId AND c.deleted=false"
            , nativeQuery = true)
    List<Car> findCarsByUserId(@Param("userId") UUID userId);

    List<Car> findByPhoneContaining(String phone);
    @Query(value = "DELETE FROM cars", nativeQuery = true)
    @Modifying
    void deleteAll();
}
