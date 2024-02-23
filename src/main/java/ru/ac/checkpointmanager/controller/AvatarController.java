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

import static ru.ac.checkpointmanager.utils.SwaggerConstants.AVATAR_NOT_FOUND_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.BAD_REQUEST_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.INTERNAL_SERVER_ERROR_MSG;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.UNAUTHORIZED_MSG;


@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("api/v1/avatars")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Аватары для объектов", description = "User и Territory")
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = UNAUTHORIZED_MSG,
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "500", description = INTERNAL_SERVER_ERROR_MSG,
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public class AvatarController {

    private final AvatarService avatarService;

    @Operation(summary = "Загрузить аватар пользователю(выбрать id пользователя и картинку).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Аватар успешно добавлен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AvatarDTO.class))),
            @ApiResponse(responseCode = "400", description = BAD_REQUEST_MESSAGE,
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY') or @userAuthFacade.isIdMatch(#userId)")
    @PostMapping(value = "/users/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AvatarDTO uploadAvatar(@PathVariable UUID userId,
                                  @RequestPart @AvatarImageCheck MultipartFile avatarFile) {
        return avatarService.uploadAvatar(userId, avatarFile);
    }

    @Operation(summary = "Получить аватар по Id пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аватар получен. Контент содержит изображение в формате JPEG.",
                    content = @Content(mediaType = MediaType.IMAGE_JPEG_VALUE,
                            schema = @Schema(implementation = byte[].class))),
            @ApiResponse(responseCode = "404", description = AVATAR_NOT_FOUND_MESSAGE,
                    content = @Content(schema = @Schema(implementation = AvatarNotFoundException.class))),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/users/{userId}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable UUID userId) {
        AvatarImageDTO avatarImageDTO = avatarService.getAvatarByUserId(userId);
        return createResponseEntity(avatarImageDTO);
    }

    @Operation(summary = "Удалить аватар по Id пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Аватар удален."),
            @ApiResponse(responseCode = "404", description = AVATAR_NOT_FOUND_MESSAGE,
                    content = @Content(schema = @Schema(implementation = AvatarNotFoundException.class))),
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY') or @userAuthFacade.isIdMatch(#userId)")
    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAvatarByUserId(@PathVariable UUID userId) {
        avatarService.deleteAvatarByUserId(userId);
    }

    @Operation(summary = "Удалить аватара по Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Аватар удален."),
            @ApiResponse(responseCode = "404", description = AVATAR_NOT_FOUND_MESSAGE,
                    content = @Content(schema = @Schema(implementation = AvatarNotFoundException.class))),
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY') or @avatarAuthFacade.isIdMatch(#avatarId)")
    @DeleteMapping("/{avatarId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAvatar(@PathVariable UUID avatarId) {
        avatarService.deleteAvatarIfExists(avatarId);
    }

    @Operation(summary = "Получить аватар по Id аватара.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аватар получен. Контент содержит изображение в формате JPEG.",
                    content = @Content(mediaType = MediaType.IMAGE_JPEG_VALUE,
                            schema = @Schema(implementation = byte[].class))),
            @ApiResponse(responseCode = "404", description = AVATAR_NOT_FOUND_MESSAGE,
                    content = @Content(schema = @Schema(implementation = AvatarNotFoundException.class))),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/{avatarId}")
    public ResponseEntity<byte[]> getAvatarById(@PathVariable UUID avatarId) {
        AvatarImageDTO avatarImageDTO = avatarService.getAvatarImageByAvatarId(avatarId);
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
            @ApiResponse(responseCode = "404", description = AVATAR_NOT_FOUND_MESSAGE,
                    content = @Content(schema = @Schema(implementation = AvatarNotFoundException.class))),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/fromSofa/{userId}")
    public ResponseEntity<AvatarImageDTO> getAvatarByUserId(@PathVariable UUID userId) {
        AvatarImageDTO avatarImageDTO = avatarService.getAvatarByUserId(userId);
        return ResponseEntity.ok(avatarImageDTO);
    }

    @Operation(summary = "Загрузить аватар территории(выбрать id территории и картинку).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Аватар успешно добавлен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AvatarDTO.class))),
            @ApiResponse(responseCode = "400", description = BAD_REQUEST_MESSAGE,
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    @PostMapping(value = "/territories/{territoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AvatarDTO uploadAvatarByTerritory(@PathVariable UUID territoryId,
                                             @RequestPart @AvatarImageCheck MultipartFile avatarFile) {
        return avatarService.uploadAvatarByTerritory(territoryId, avatarFile);
    }

    @Operation(summary = "Получить аватар по Id территории")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аватар получен. Контент содержит изображение в формате JPEG.",
                    content = @Content(mediaType = MediaType.IMAGE_JPEG_VALUE,
                            schema = @Schema(implementation = byte[].class))),
            @ApiResponse(responseCode = "404", description = AVATAR_NOT_FOUND_MESSAGE,
                    content = @Content(schema = @Schema(implementation = AvatarNotFoundException.class))),
    })
    @GetMapping("/territories/{territoryId}")
    public ResponseEntity<byte[]> getAvatarByTerritory(@PathVariable UUID territoryId) {
        AvatarImageDTO avatarImageDTO = avatarService.getAvatarImageByTerritoryId(territoryId);
        return createResponseEntity(avatarImageDTO);
    }

}
