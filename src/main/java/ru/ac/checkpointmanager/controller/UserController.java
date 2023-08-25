package ru.ac.checkpointmanager.controller;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.service.UserService;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;

    @Autowired
    public UserController(UserService userService, ModelMapper modelMapper) {
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/authentication")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>("Validation error", HttpStatus.BAD_REQUEST);
        }

        User createdUser = userService.createUser(convertToUser(userDTO));
        return new ResponseEntity<>(convertToUserDTO(createdUser), HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<UserDTO> findUserById(@PathVariable UUID id) {
        Optional<UserDTO> user = Optional.ofNullable(convertToUserDTO(userService.findById(id)));
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @GetMapping(params = "{name}")
    public ResponseEntity<Collection<UserDTO>> findUserByName(@RequestParam(required = false) String name) {
        Collection<UserDTO> foundUsers = userService.findByName(name).stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
        return foundUsers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(foundUsers);
    }

    @GetMapping
    public ResponseEntity<Collection<UserDTO>> getAll() {
        Collection<UserDTO> foundUsers = userService.getAll().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
        return foundUsers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(foundUsers);
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>("Validation error", HttpStatus.BAD_REQUEST);
        }

        User user = userService.findByEmail(userDTO.getEmail());
        User userToUpdate = convertToUser(userDTO);
        userToUpdate.setId(user.getId());

        User changedUser = userService.updateUser(userToUpdate);
        return user == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(convertToUserDTO(changedUser));
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

    private User convertToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    private UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

}
