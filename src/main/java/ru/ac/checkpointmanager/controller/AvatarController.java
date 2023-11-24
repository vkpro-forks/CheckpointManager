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
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.avatar.AvatarDTO;
import ru.ac.checkpointmanager.dto.avatar.AvatarImageDTO;
import ru.ac.checkpointmanager.exception.AvatarNotFoundException;
import ru.ac.checkpointmanager.service.avatar.AvatarService;

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

    @Operation(summary = "Загрузить аватар пользователю(выбрать id пользователя и картинку).")
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
        AvatarDTO avatarDTO = service.uploadAvatar(userId, avatarFile);
        return ResponseEntity.ok(avatarDTO);
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
    public ResponseEntity<byte[]> getAvatar(@PathVariable UUID userId) {
        AvatarImageDTO avatarImageDTO = service.getAvatarByUserId(userId);
        return createResponseEntity(avatarImageDTO);
    }

    @Operation(summary = "Удалить аватара по Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аватар получен. Контент содержит изображение в формате JPEG."),
            @ApiResponse(responseCode = "404", description = "NOT_FOUND: Аватар не найден",
                    content = @Content(schema = @Schema(implementation = AvatarNotFoundException.class))),
    })
    @DeleteMapping("/{avatarId}")
    public void deleteAvatar(@PathVariable UUID avatarId) {
        service.deleteAvatarIfExists(avatarId);
    }

    @Operation(summary = "Получить аватар по Id аватара.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аватар получен. Контент содержит изображение в формате JPEG.",
                    content = @Content(mediaType = MediaType.IMAGE_JPEG_VALUE,
                            schema = @Schema(implementation = byte[].class))),
            @ApiResponse(responseCode = "404", description = "NOT_FOUND: Аватар не найден",
                    content = @Content(schema = @Schema(implementation = AvatarNotFoundException.class))),
    })
    @GetMapping("/avatars/{avatarId}")
    public ResponseEntity<byte[]> getAvatarById(@PathVariable UUID avatarId) {
        AvatarImageDTO avatarImageDTO = service.getAvatarImageByAvatarId(avatarId);
        return createResponseEntity(avatarImageDTO);
    }


    private ResponseEntity<byte[]> createResponseEntity(AvatarImageDTO avatarImageDTO) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(avatarImageDTO.getMediaType()));
        headers.setContentLength(avatarImageDTO.getImageData().length);
        return ResponseEntity.ok().headers(headers).body(avatarImageDTO.getImageData());
    }
}
