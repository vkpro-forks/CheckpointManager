package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.model.Person;

import java.util.UUID;

public interface PersonService {

    Person addPerson(Person person);

    Person getPerson(UUID uuid);

    Person updatePerson(UUID uuid, Person person);

    void deletePerson(UUID uuid);


}
