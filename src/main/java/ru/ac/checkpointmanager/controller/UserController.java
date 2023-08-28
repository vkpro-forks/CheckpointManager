package ru.ac.checkpointmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.service.UserService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping("/authentication")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }
        User createdUser = userService.createUser(userService.convertToUser(userDTO));
        return new ResponseEntity<>(userService.convertToUserDTO(createdUser), HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<UserDTO> findUserById(@PathVariable UUID id) {
        Optional<UserDTO> user = Optional.ofNullable(userService.convertToUserDTO(userService.findById(id)));
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @GetMapping(params = "{name}")
    public ResponseEntity<Collection<UserDTO>> findUserByName(@RequestParam(required = false) String name) {
        Collection<UserDTO> foundUsers = userService.findByName(name).stream()
                .map(userService::convertToUserDTO)
                .collect(Collectors.toList());
        return foundUsers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(foundUsers);
    }

    @GetMapping
    public ResponseEntity<Collection<UserDTO>> getAll() {
        Collection<UserDTO> foundUsers = userService.getAll().stream()
                .map(userService::convertToUserDTO)
                .collect(Collectors.toList());
        return foundUsers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(foundUsers);
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        User user = userService.findByEmail(userDTO.getEmail());
        User userToUpdate = userService.convertToUser(userDTO);
        userToUpdate.setId(user.getId());

        User changedUser = userService.updateUser(userToUpdate);
        return changedUser == null ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(userService.convertToUserDTO(changedUser));
    }

    //    method with limited access
    //    1 variate
    @PatchMapping("{id}")
    public ResponseEntity<User> updateBlockStatus(@PathVariable UUID id, @RequestParam Boolean isBlocked) {
        User changedUser = userService.updateBlockStatus(id, isBlocked);
        return changedUser == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(changedUser);
    }

    //    method with limited access
    //    2 variate
    @PatchMapping("/block/{id}")
    public ResponseEntity<?> blockById(@PathVariable UUID id) {
        try {
            userService.blockById(id);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    //    method with limited access
    //    2 variate
    @PatchMapping("/unblock/{id}")
    public ResponseEntity<?> unblockById(@PathVariable UUID id) {
        try {
            userService.unblockById(id);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }
//choose variate witch best for frontend

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        User foundUser = userService.findById(id);
        if (foundUser == null) {
            return ResponseEntity.noContent().build();
        }
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
