package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.AvatarDTO;
import ru.ac.checkpointmanager.exception.AvatarNotFoundException;
import ru.ac.checkpointmanager.mapper.AvatarMapper;
import ru.ac.checkpointmanager.model.Avatar;
import ru.ac.checkpointmanager.service.avatar.AvatarService;
import ru.ac.checkpointmanager.service.user.UserService;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chpman/avatars")
@SecurityRequirement(name = "bearerAuth")
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "UNAUTHORIZED: пользователь не авторизован",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "FORBIDDEN: доступ запрещен",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
public class AvatarController {

    private final AvatarService service;
    private final UserService userService;
    private final AvatarMapper avatarMapper;

    @Operation(summary = "Добавить новый аватар.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аватар успешно добавлен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AvatarDTO.class))),
            @ApiResponse(responseCode = "400", description = "BAD_REQUEST: Неверные данные запроса",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    @PostMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AvatarDTO> uploadAvatar(@PathVariable UUID userId,
                                               @RequestBody MultipartFile avatarFile) {
        try {
            Avatar avatar = service.uploadAvatar(userId, avatarFile);

            if (avatar.getPreview() == null) {
                log.warn("Preview image is null after uploading avatar");
            } else {
                log.info("Preview image size: {}", avatar.getPreview().length);
            }

            AvatarDTO avatarDTO = avatarMapper.toAvatarDTO(avatar);
            userService.assignAvatarToUser(userId, avatar);
            return ResponseEntity.ok(avatarDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Получить аватар по Id пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аватар получен. Контент содержит изображение в формате JPEG.",
                    content = @Content(mediaType = MediaType.IMAGE_JPEG_VALUE,
                            schema = @Schema(implementation = byte[].class))),
            @ApiResponse(responseCode = "404", description = "NOT_FOUND: Аватар не найден",
                    content = @Content(schema = @Schema(implementation = AvatarNotFoundException.class))),
    })
    @GetMapping("/{userId}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable UUID userId) throws IOException {
        byte[] imageData = service.getAvatarByUserId(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(imageData.length);

        return ResponseEntity.ok().headers(headers).body(imageData);
    }

    @Operation(summary = "Удалить аватара по Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аватар удален"),
            @ApiResponse(responseCode = "404", description = "NOT_FOUND: Аватар не найден",
                    content = @Content(schema = @Schema(implementation = AvatarNotFoundException.class))),
    })
    @DeleteMapping("/{avatarId}")
    public void deleteAvatar(@PathVariable UUID avatarId) throws IOException {
        service.deleteAvatarIfExists(avatarId);
    }
}
