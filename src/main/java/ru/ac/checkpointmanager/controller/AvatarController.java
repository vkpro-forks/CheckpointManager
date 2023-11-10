package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.exception.AvatarNotFoundException;
import ru.ac.checkpointmanager.model.Avatar;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.service.avatar.AvatarService;
import ru.ac.checkpointmanager.service.user.UserService;
import ru.ac.checkpointmanager.utils.Mapper;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chpman/avatars")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = "UNAUTHORIZED: пользователь не авторизован"),
        @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса")})
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
public class AvatarController {

    private final AvatarService service;
    private final UserService userService;
    private final Mapper mapper;

    @Operation(summary = "Загрузить аватар пользователю(выбрать id пользователя и картинку).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аватар успешно загружен"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос, проверьте данные"),
    })
    @PostMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAvatar(@PathVariable UUID userId,
                                               @RequestBody MultipartFile avatarFile) throws IOException {
        try {
            Avatar avatar = service.uploadAvatar(userId, avatarFile);
            userService.assignAvatarToUser(userId, avatar);
            return ResponseEntity.ok("Аватар загружен и назначен пользователю.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка при загрузке аватара: " + e.getMessage());
        }
    }

    @Operation(summary = "Получить аватар по Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аватар получен"),
            @ApiResponse(responseCode = "404", description = "Аватар не найден"),
    })
    @GetMapping("/{entityID}")
    public void getAvatar(@PathVariable UUID entityID,
                          HttpServletResponse response) throws IOException {
        service.getAvatar(entityID);
    }
    @Operation(summary = "Получить привью аватара по Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Привью получено"),
            @ApiResponse(responseCode = "404", description = "Аватар не найден"),
    })
    @GetMapping("/preview/{entityID}")
    public ResponseEntity<byte[]> getAvatarPreview(@PathVariable UUID entityID) {
        Avatar avatar = service.findAvatarOrThrow(entityID);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(avatar.getMediaType()));
        headers.setContentLength(avatar.getPreview().length);
        return ResponseEntity.ok().headers(headers).body(avatar.getPreview());
    }

    @Operation(summary = "Удалить аватара по Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аватар удален"),
            @ApiResponse(responseCode = "404", description = "Аватар не найден"),
    })
    @DeleteMapping("/{entityID}")
    public void deleteAvatar(@PathVariable UUID entityID) {
        if (service.deleteAvatarIfExists(entityID) == null) {
            throw new AvatarNotFoundException("Entity with id = " + entityID + " has no avatar");
        }
    }
}
