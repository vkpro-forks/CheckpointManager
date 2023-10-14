package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.ChangePasswordRequest;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.service.UserService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("chpman/user")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("{id}")
    public ResponseEntity<UserDTO> findUserById(@PathVariable UUID id) {
        Optional<UserDTO> user = Optional.ofNullable(userService.findById(id));
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/{userId}/territories")
    public ResponseEntity<List<TerritoryDTO>> getTerritoriesByUser(@PathVariable UUID userId) {
        List<TerritoryDTO> territories = userService.findTerritoriesByUserId(userId);
        return territories.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(territories);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/name")
    public ResponseEntity<Collection<UserDTO>> findUserByName(@RequestParam String name) {
        Collection<UserDTO> foundUsers = userService.findByName(name);
        return foundUsers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(foundUsers);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping()
    public ResponseEntity<Collection<UserDTO>> getAll() {
        Collection<UserDTO> foundUsers = userService.getAll();
        return foundUsers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(foundUsers);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/numbers/{id}")
    public ResponseEntity<Collection<String>> findUsersPhoneNumbers(@PathVariable UUID id) {
        Collection<String> numbers = userService.findUsersPhoneNumbers(id);
        return numbers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(numbers);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @PutMapping
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }
        UserDTO changedUser = userService.updateUser(userDTO);
        return new ResponseEntity<>(changedUser, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest request,
                                            BindingResult result,
                                            Principal connectedUser) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        userService.changePassword(request, connectedUser);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("/role/{id}")
    public ResponseEntity<?> changeRole(@PathVariable UUID id, @RequestParam Role role, Principal connectedUser) {
        userService.changeRole(id, role, connectedUser);
        return ResponseEntity.ok().build();
    }

    //    method with limited access
    //    1 variate
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("{id}")
    public ResponseEntity<?> updateBlockStatus(@PathVariable UUID id, @RequestParam Boolean isBlocked) {
        UserDTO changedUser = userService.updateBlockStatus(id, isBlocked);
        return ResponseEntity.ok(changedUser);
    }

    //    method with limited access
    //    2 variate
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("/block/{id}")
    public ResponseEntity<?> blockById(@PathVariable UUID id) {
        userService.blockById(id);
        return ResponseEntity.ok().build();
    }

    //    method with limited access
    //    2 variate
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("/unblock/{id}")
    public ResponseEntity<?> unblockById(@PathVariable UUID id) {
        userService.unblockById(id);
        return ResponseEntity.ok().build();
    }
    //choose variate witch best for frontend

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER')")
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
