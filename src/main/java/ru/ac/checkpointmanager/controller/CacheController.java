package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.service.cache.CacheService;

@RestController
@RequestMapping("api/v1/cache")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Управление кэшем", description = "Управление кэшем пока что ВСЕГО приложения")
@ApiResponses(value = {
        @ApiResponse(responseCode = "401",
                description = "UNAUTHORIZED: пользователь не авторизован"),
        @ApiResponse(responseCode = "500",
                description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса")})
public class CacheController {

    private final CacheService cacheService;

    @Operation(summary = "Очистить весь кэш",
            description = "Доступ: ADMIN"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: кэш очищен"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "FORBIDDEN: роль пользователя не предоставляет доступ к данному api"
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping
    public ResponseEntity<?> clearCache() {
        cacheService.clearAllCaches();
        return ResponseEntity.ok().build();
    }

//TODO: функционал для очистки кэша, принадлежащего юзеру
}
