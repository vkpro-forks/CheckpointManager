package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.model.Person;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonService {

    Person addPerson(Person person);

    Person getPerson(UUID uuid);

    Person updatePerson(UUID uuid, Person person);

    void deletePerson(UUID uuid);

    List<Person> findByNamePart(String name);

    List<Person> findByPhonePart(String phone);

    Optional<Person> findByPassId(UUID passId);

}
