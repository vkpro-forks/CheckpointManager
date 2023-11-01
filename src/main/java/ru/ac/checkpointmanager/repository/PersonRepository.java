package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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

}
