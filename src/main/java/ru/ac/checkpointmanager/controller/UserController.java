package ru.ac.checkpointmanager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.service.UserService;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/authentication")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        if (createdUser == null) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("{id}")
    public ResponseEntity<User> findUserById(@PathVariable UUID id) {
        Optional<User> user = Optional.ofNullable(userService.findById(id));
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @GetMapping(params = "{name}")
    public ResponseEntity<Collection<User>> findUserByName(@RequestParam String name) {
        Collection<User> foundUsers = userService.findByName(name);
        return foundUsers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(foundUsers);
    }

    @GetMapping
    public ResponseEntity<Collection<User>> getAll() {
        Collection<User> foundUsers = userService.getAll();
        return foundUsers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(foundUsers);
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        User changedUser = userService.updateUser(user);
        return changedUser == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(changedUser);
    }

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
