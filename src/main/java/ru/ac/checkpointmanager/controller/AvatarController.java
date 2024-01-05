package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.dto.avatar.AvatarDTO;
import ru.ac.checkpointmanager.dto.avatar.AvatarImageDTO;
import ru.ac.checkpointmanager.exception.AvatarNotFoundException;
import ru.ac.checkpointmanager.service.avatar.AvatarService;
import ru.ac.checkpointmanager.validation.annotation.AvatarImageCheck;

import java.util.UUID;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("api/v1/avatars")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Аватары для объектов", description = "User и Territory")
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = "UNAUTHORIZED: пользователь не авторизован",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса",
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
public class AvatarController {

    private final AvatarService service;

    @Operation(summary = "Загрузить аватар пользователю(выбрать id пользователя и картинку).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Аватар успешно добавлен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AvatarDTO.class))),
            @ApiResponse(responseCode = "400", description = "BAD_REQUEST: Неверные данные запроса",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    @PostMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AvatarDTO uploadAvatar(@PathVariable UUID userId,
                                  @RequestPart @AvatarImageCheck MultipartFile avatarFile) {
        return service.uploadAvatar(userId, avatarFile);
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
            @ApiResponse(responseCode = "204", description = "Аватар удален."),
            @ApiResponse(responseCode = "404", description = "NOT_FOUND: Аватар не найден",
                    content = @Content(schema = @Schema(implementation = AvatarNotFoundException.class))),
    })
    @DeleteMapping("/{avatarId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
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

    @Operation(summary = "Получить аватар по Id пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аватар получен. Контент содержит изображение в формате JPEG.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AvatarImageDTO.class))),
            @ApiResponse(responseCode = "404", description = "NOT_FOUND: Аватар не найден",
                    content = @Content(schema = @Schema(implementation = AvatarNotFoundException.class))),
    })
    @GetMapping("/fromSofa/{userId}")
    public ResponseEntity<AvatarImageDTO> getAvatarByUserId(@PathVariable UUID userId) {
        AvatarImageDTO avatarImageDTO = service.getAvatarByUserId(userId);
        return ResponseEntity.ok(avatarImageDTO);
    }

    @Operation(summary = "Загрузить аватар территории(выбрать id территории и картинку).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Аватар успешно добавлен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AvatarDTO.class))),
            @ApiResponse(responseCode = "400", description = "BAD_REQUEST: Неверные данные запроса",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    @PostMapping(value = "/{territoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AvatarDTO uploadAvatarByTerritory(@PathVariable UUID territoryId,
                                  @RequestPart @AvatarImageCheck MultipartFile avatarFile) {
        return service.uploadAvatar(territoryId, avatarFile);
    }

    @Operation(summary = "Получить аватар по Id территории")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аватар получен. Контент содержит изображение в формате JPEG.",
                    content = @Content(mediaType = MediaType.IMAGE_JPEG_VALUE,
                            schema = @Schema(implementation = byte[].class))),
            @ApiResponse(responseCode = "404", description = "NOT_FOUND: Аватар не найден",
                    content = @Content(schema = @Schema(implementation = AvatarNotFoundException.class))),
    })
    @GetMapping("/{territoryId}")
    public ResponseEntity<byte[]> getAvatarByTerritory(@PathVariable UUID territoryId) {
        AvatarImageDTO avatarImageDTO = service.getAvatarByUserId(territoryId);
        return createResponseEntity(avatarImageDTO);
    }
}
