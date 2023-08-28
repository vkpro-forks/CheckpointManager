package ru.ac.checkpointmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.ac.checkpointmanager.dto.PhoneNumberDTO;
import ru.ac.checkpointmanager.exception.PhoneNumberNotFoundException;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.PhoneNumber;
import ru.ac.checkpointmanager.service.PhoneNumberService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/number")
@RequiredArgsConstructor
public class PhoneNumberController {

    private final PhoneNumberService numberService;


    @PostMapping
    public ResponseEntity<?> createPhoneNumber(@Valid @RequestBody PhoneNumberDTO numberDTO,
                                               BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        try {
            PhoneNumber number = numberService.createPhoneNumber(numberService.convertToPhoneNumber(numberDTO));
            return new ResponseEntity<>(numberService.convertToPhoneNumberDTO(number), HttpStatus.CREATED);
        } catch (UserNotFoundException e) { /* обрабатываем исключение из сервиса */
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<PhoneNumberDTO> findById(@PathVariable UUID id) {
        Optional<PhoneNumberDTO> foundNumber = Optional.ofNullable(numberService.convertToPhoneNumberDTO(numberService.findById(id)));
        return foundNumber.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @GetMapping
    public ResponseEntity<Collection<PhoneNumberDTO>> getAll() {
        Collection<PhoneNumberDTO> numbers = numberService.getAll().stream()
                .map(numberService::convertToPhoneNumberDTO)
                .collect(Collectors.toList());
        return numbers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(numbers);
    }

    @PutMapping
    public ResponseEntity<?> updateNumber(@Valid @RequestBody PhoneNumberDTO numberDTO,
                                          BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        PhoneNumber numberToUpdate = numberService.convertToPhoneNumber(numberDTO);
        try {
            if (numberService.findById(numberToUpdate.getId()) == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID does not match with existing phone numbers");
            }

            PhoneNumber changedNumber = numberService.updatePhoneNumber(numberToUpdate);
            return ResponseEntity.ok(numberService.convertToPhoneNumberDTO(changedNumber));

        } catch (PhoneNumberNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteNumber(@PathVariable UUID id) {
        PhoneNumber foundNumber = numberService.findById(id);
        if (foundNumber == null) {
            return ResponseEntity.noContent().build();
        }
        numberService.deletePhoneNumber(id);
        return ResponseEntity.ok().build();
    }
}
