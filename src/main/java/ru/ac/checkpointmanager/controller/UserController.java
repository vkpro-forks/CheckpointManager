package ru.ac.checkpointmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.UserAuthDTO;
import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.service.UserService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("{id}")
    public ResponseEntity<UserDTO> findUserById(@PathVariable UUID id) {
        Optional<UserDTO> user = Optional.ofNullable(userService.findById(id));
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/{userId}/territories")
    public ResponseEntity<List<TerritoryDTO>> getTerritoriesByUser(@PathVariable UUID userId) {
        List<TerritoryDTO> territories = userService.findTerritoriesByUserId(userId);
        return territories.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(territories);
    }

    @GetMapping("/name")
    public ResponseEntity<Collection<UserDTO>> findUserByName(@RequestParam String name) {
        Collection<UserDTO> foundUsers = userService.findByName(name);
        return foundUsers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(foundUsers);
    }

    @GetMapping()
    public ResponseEntity<Collection<UserDTO>> getAll() {
        Collection<UserDTO> foundUsers = userService.getAll();
        return foundUsers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(foundUsers);
    }

    @GetMapping("/numbers/{id}")
    public ResponseEntity<Collection<String>> findUsersPhoneNumbers(@PathVariable UUID id) {
        Collection<String> numbers = userService.findUsersPhoneNumbers(id);
        return numbers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(numbers);
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserAuthDTO userAuthDTO, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        try {
            UserAuthDTO changedUser = userService.updateUser(userAuthDTO);
            return new ResponseEntity<>(changedUser, HttpStatus.OK);

        } catch (RuntimeException e) {
            // блок отлавливает эксепшены и пишет месседжы, которые прописаны в сервисе
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //    method with limited access
    //    1 variate
    @PatchMapping("{id}")
    public ResponseEntity<?> updateBlockStatus(@PathVariable UUID id, @RequestParam Boolean isBlocked) {
        try {
            UserDTO changedUser = userService.updateBlockStatus(id, isBlocked);
            return ResponseEntity.ok(changedUser);
        } catch (UserNotFoundException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //    method with limited access
    //    2 variate
    @PatchMapping("/block/{id}")
    public ResponseEntity<?> blockById(@PathVariable UUID id) {
        try {
            userService.blockById(id);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //    method with limited access
    //    2 variate
    @PatchMapping("/unblock/{id}")
    public ResponseEntity<?> unblockById(@PathVariable UUID id) {
        try {
            userService.unblockById(id);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    //choose variate witch best for frontend

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        UserDTO foundUser = userService.findById(id);
        if (foundUser == null) {
            return ResponseEntity.noContent().build();
        }
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}
