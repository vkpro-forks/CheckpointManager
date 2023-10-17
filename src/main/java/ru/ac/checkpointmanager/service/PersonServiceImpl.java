package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.PersonNotFoundException;
import ru.ac.checkpointmanager.model.Person;
import ru.ac.checkpointmanager.repository.PersonRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;

    @Override
    public Person addPerson(Person person) {
        if (person == null) {
            log.warn("Attempt to add null Person");
            throw new IllegalArgumentException("Person cannot be null");
        }
        log.info("Adding new Person: {}", person);
        return personRepository.save(person);
    }

    @Override
    public Person getPerson(UUID uuid) {
        if (uuid == null) {
            log.warn("Attempt to get Person with null UUID");
            throw new IllegalArgumentException("UUID cannot be null");
        }
        Optional<Person> personOptional = personRepository.findById(uuid);
        return personOptional.orElseThrow(() -> {
            log.warn("Person not found for UUID: {}", uuid);
            return new PersonNotFoundException("Person not found");
        });
    }

    @Override
    public Person updatePerson(UUID uuid, Person person) {
        if (uuid == null || person == null) {
            log.warn("Attempt to update Person with null UUID or null Person");
            throw new IllegalArgumentException("UUID or Person cannot be null");
        }
        Person existPerson = getPerson(uuid);
        log.info("Updating Person with UUID: {}, new data: {}", uuid, person);
        existPerson.setName(person.getName());
        existPerson.setPhone(person.getPhone());
        existPerson.setNote(person.getNote());
        return personRepository.save(existPerson);
    }

    @Override
    public void deletePerson(UUID uuid) {
        if (uuid == null) {
            log.warn("Attempt to delete Person with null UUID");
            throw new IllegalArgumentException("UUID cannot be null");
        }
        Person existPerson = getPerson(uuid);
        log.info("Deleting Person with UUID: {}", uuid);
        personRepository.delete(existPerson);
    }
}
