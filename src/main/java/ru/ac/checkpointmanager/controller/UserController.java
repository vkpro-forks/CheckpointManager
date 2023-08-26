package ru.ac.checkpointmanager.controller;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.exception.UserNotFoundException;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.service.UserService;

import java.util.Collection;
import java.util.List;
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
            return new ResponseEntity<>(errorsList(result), HttpStatus.BAD_REQUEST);
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
            return new ResponseEntity<>(errorsList(result), HttpStatus.BAD_REQUEST);
        }

        User user = userService.findByEmail(userDTO.getEmail());
        User userToUpdate = convertToUser(userDTO);
        userToUpdate.setId(user.getId());

        User changedUser = userService.updateUser(userToUpdate);
        return user == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(convertToUserDTO(changedUser));
    }

    //    method with limited access
    @PatchMapping("{id}")
    public ResponseEntity<User> updateBlockStatus(@PathVariable UUID id, @RequestParam Boolean isBlocked) {
        User changedUser = userService.updateBlockStatus(id, isBlocked);
        return changedUser == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(changedUser);
    }

    //    method with limited access
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

    private String errorsList(BindingResult bindingResult) {
        StringBuilder errorMsg = new StringBuilder();
        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            errorMsg.append(error.getField())
                    .append(" - ")
                    .append(error.getDefaultMessage())
                    .append("\n");
        }
        return errorMsg.toString();
    }
}
