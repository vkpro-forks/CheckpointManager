package ru.ac.checkpointmanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/authentication")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userService.createUser(user));
    }

    @GetMapping("{id}")
    public ResponseEntity<User> findUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<Collection<User>> findUserByName(@RequestParam String name) {
        Collection<User> users = userService.findByName(name);
        if (users.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(users);
    }
}
