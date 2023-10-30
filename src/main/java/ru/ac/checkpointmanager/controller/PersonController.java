package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.model.Person;
import ru.ac.checkpointmanager.service.person.PersonService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("chpman/person")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
public class PersonController {

    private final PersonService personService;

    @PostMapping
    public ResponseEntity<?> addPerson(@Valid @RequestBody Person person, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }
        Person newPerson = personService.addPerson(person);
        return new ResponseEntity<>(newPerson, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPerson(@PathVariable UUID id) {
        Person existPerson = personService.getPerson(id);
        return new ResponseEntity<>(existPerson, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePerson(@PathVariable UUID id, @RequestBody Person person,
                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Person updatePerson = personService.updatePerson(id, person);
        return new ResponseEntity<>(updatePerson, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePerson(@PathVariable UUID id) {
        personService.deletePerson(id);
        return new ResponseEntity<>("Person deleted " + id, HttpStatus.OK);
    }

    @GetMapping("/phone")
    public ResponseEntity<List<Person>> searchByPhone(@RequestParam String phone) {
        List<Person> persons = personService.findByPhonePart(phone);
        return new ResponseEntity<>(persons, HttpStatus.OK);
    }

    @GetMapping("/name")
    public ResponseEntity<List<Person>> searchByName(@RequestParam String name) {
        List<Person> persons = personService.findByNamePart(name);
        return new ResponseEntity<>(persons, HttpStatus.OK);
    }

    @GetMapping("/pass")
    public ResponseEntity<?> searchByPass(@RequestParam UUID uuid) {
        Person existPerson = personService.findByPassId(uuid).orElse(null);
        if (uuid != null) {
            return new ResponseEntity<>(existPerson, HttpStatus.OK);
        }
        return new ResponseEntity<>("There is no such person in any pass! ", HttpStatus.NOT_FOUND);
    }
}
