package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ac.checkpointmanager.model.Phone;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface PhoneRepository extends JpaRepository<Phone, UUID> {

    Boolean existsByNumber(String number);

    Optional<Phone> findByNumber(String number);

    // ищет в таблице phones номера, которые привязаны к переданному user_id
    @Query(value = "SELECT number FROM phones WHERE user_id = :user_id", nativeQuery = true)
    Collection<String> getNumbersByUserId(@Param("user_id") UUID id);
}
