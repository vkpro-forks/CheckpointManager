package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ac.checkpointmanager.model.Person;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonRepository extends JpaRepository<Person, UUID> {

    List<Person> findByNameContainingIgnoreCase(String name);
    List<Person> findByPhoneContaining(String phone);
    Optional<Person> findPersonByPasses_Id(UUID passId);
    Optional<Person> findByPhone(String name);

    @Query(value = "SELECT p.* FROM persons p JOIN passes p2 ON p.id = p2.person_id WHERE p2.user_id = :userId"
            , nativeQuery = true)
    List<Person> findPersonsByUserId(@Param("userId") UUID userId);

}
