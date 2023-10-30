package ru.ac.checkpointmanager.service.person;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.PersonNotFoundException;
import ru.ac.checkpointmanager.model.Person;
import ru.ac.checkpointmanager.repository.PersonRepository;

import java.util.List;
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

    @Override
    public List<Person> findByNamePart(String name) {
        if (name == null || name.isEmpty()) {
            log.warn("Attempt to find Person by null or empty name");
            throw new IllegalArgumentException("Name part cannot be null or empty");
        }
        log.info("Searching for Persons with name containing: {}", name);
        return personRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Person> findByPhonePart(String phone) {
        if (phone == null || phone.isEmpty()) {
            log.warn("Attempt to find Person by null or empty phone");
            throw new IllegalArgumentException("Phone part cannot be null or empty");
        }
        log.info("Searching for Persons with phone containing: {}", phone);
        return personRepository.findByPhoneContaining(phone);
    }

    @Override
    public Optional<Person> findByPassId(UUID passId) {
        if (passId == null) {
            log.warn("Attempt to find Person by null passId");
            throw new IllegalArgumentException("Pass ID cannot be null");
        }
        log.info("Searching for Person with Pass ID: {}", passId);
        return personRepository.findPersonByPasses_Id(passId);
    }
}
