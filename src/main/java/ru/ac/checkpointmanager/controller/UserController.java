package ru.ac.checkpointmanager.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.service.UserService;

import javax.validation.Valid;
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
    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid UserDTO userDTO) {
        User createdUser = userService.createUser(convertToUser(userDTO));
        return ResponseEntity.ok(convertToUserDTO(createdUser));
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
    public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO userDTO) {
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
