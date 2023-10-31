package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.exception.PhoneNumberNotFoundException;
import ru.ac.checkpointmanager.service.phone.PhoneService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("chpman/phone")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
public class PhoneController {

    private final PhoneService phoneService;

    @PostMapping
    public ResponseEntity<?> createPhoneNumber(@Valid @RequestBody PhoneDTO phoneDTO,
                                               BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        PhoneDTO createdPhone = phoneService.createPhoneNumber(phoneDTO);
        return new ResponseEntity<>(createdPhone, HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<PhoneDTO> findById(@PathVariable UUID id) {
        Optional<PhoneDTO> foundPhone = Optional.ofNullable(phoneService.findById(id));
        return foundPhone.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @GetMapping
    public ResponseEntity<Collection<PhoneDTO>> getAll() {
        Collection<PhoneDTO> phones = phoneService.getAll();
        return phones.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(phones);
    }

    @PutMapping
    public ResponseEntity<?> updateNumber(@Valid @RequestBody PhoneDTO phoneDTO,
                                          BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        try {
            PhoneDTO changedNumber = phoneService.updatePhoneNumber(phoneDTO);
            return ResponseEntity.ok(changedNumber);
        } catch (PhoneNumberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteNumber(@PathVariable UUID id) {
        PhoneDTO foundPhone = phoneService.findById(id);
        if (foundPhone == null) {
            return ResponseEntity.noContent().build();
        }
        phoneService.deletePhoneNumber(id);
        return ResponseEntity.ok().build();
    }
}
