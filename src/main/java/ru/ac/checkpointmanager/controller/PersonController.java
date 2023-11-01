package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.PersonDTO;
import ru.ac.checkpointmanager.model.Person;
import ru.ac.checkpointmanager.service.person.PersonService;
import ru.ac.checkpointmanager.utils.ErrorUtils;
import ru.ac.checkpointmanager.utils.Mapper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("chpman/person")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
public class PersonController {

    private final PersonService personService;
    private final Mapper mapper;

    @Operation(summary = "Добавить новую личность")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Личность успешно добавлена"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @PostMapping
    public ResponseEntity<?> addPerson(@Valid @RequestBody PersonDTO personDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Person newPerson = personService.addPerson(mapper.toPerson(personDTO));
        return new ResponseEntity<>(mapper.toPersonDTO(newPerson), HttpStatus.CREATED);
    }

    @Operation(summary = "Получить личность по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Личность найдена"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getPerson(@PathVariable UUID id) {
        Person existPerson = personService.getPerson(id);
        return new ResponseEntity<>(mapper.toPersonDTO(existPerson), HttpStatus.OK);
    }

    @Operation(summary = "Обновить информацию о личности по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Личность успешно обновлена"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePerson(@PathVariable UUID id, @RequestBody PersonDTO personDTO,
                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Person person = mapper.toPerson(personDTO);
        Person updatePerson = personService.updatePerson(id, person);
        return new ResponseEntity<>(mapper.toPersonDTO(updatePerson), HttpStatus.OK);
    }

    @Operation(summary = "Удалить личность по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Личность успешно удалена"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePerson(@PathVariable UUID id) {
        personService.deletePerson(id);
        return new ResponseEntity<>("Person deleted " + id, HttpStatus.OK);
    }

    @Operation(summary = "Найти личности по номеру телефона")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Личности найдены"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping("/phone")
    public ResponseEntity<List<PersonDTO>> searchByPhone(@RequestParam String phone) {
        List<Person> persons = personService.findByPhonePart(phone);
        List<PersonDTO> personDTOS = persons.stream()
                .map(person -> mapper.toPersonDTO(person)).toList();
        return new ResponseEntity<>(personDTOS, HttpStatus.OK);
    }

    @Operation(summary = "Найти личности по имени")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Личности найдены"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping("/name")
    public ResponseEntity<List<PersonDTO>> searchByName(@RequestParam String name) {
        List<Person> persons = personService.findByNamePart(name);
        List<PersonDTO> personDTOs = persons.stream()
                .map(person -> mapper.toPersonDTO(person))
                .toList();
        return new ResponseEntity<>(personDTOs, HttpStatus.OK);
    }

    @Operation(summary = "Найти личность по ID пропуска")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Личность найдена"),
            @ApiResponse(responseCode = "404", description = "Личность не найдена"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping("/pass")
    public ResponseEntity<?> searchByPass(@RequestParam UUID uuid) {
        Person existPerson = personService.findByPassId(uuid).orElse(null);
        if (uuid != null) {
            return new ResponseEntity<>(mapper.toPersonDTO(existPerson), HttpStatus.OK);
        }
        return new ResponseEntity<>("There is no such person in any pass! ", HttpStatus.NOT_FOUND);
    }
}
