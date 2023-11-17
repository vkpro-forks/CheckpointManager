package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.passes.PassDtoCreate;
import ru.ac.checkpointmanager.dto.passes.PassDtoResponse;
import ru.ac.checkpointmanager.dto.passes.PassDtoUpdate;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.service.passes.PassService;
import ru.ac.checkpointmanager.utils.ErrorUtils;
import ru.ac.checkpointmanager.mapper.PassMapper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("chpman/pass")
@RequiredArgsConstructor
@Tag(name = "Pass (пропуска)", description = "Операции с пропусками для машин и людей")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = "UNAUTHORIZED: пользователь не авторизован"),
        @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса")})
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
public class PassController {

    private final PassService service;
    private final PassMapper mapper;

    /* CREATE */
    @Operation(summary = "Добавить новый пропуск")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск успешно добавлен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassDtoResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей; пользователь не имеет права " +
                    "создавать пропуск на эту территорию; у пользователя найден накладывающийся пропуск"),
            @ApiResponse(responseCode = "404", description = "Не найден пользователь или территория")})
    @PostMapping
    public ResponseEntity<?> addPass(@RequestBody @Valid PassDtoCreate passDTOcreate,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Pass newPass = service.addPass(mapper.toPass(passDTOcreate));
        return ResponseEntity.ok(mapper.toPassDTO(newPass));
    }

    /* READ */
    @Operation(summary = "Получить список всех пропусков")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуска найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassDtoResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Пропуска не найдены")})
    @GetMapping
    public ResponseEntity<List<PassDtoResponse>> getPasses() {
        List<Pass> passes = service.findPasses();
        if (passes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toPassDTO(passes));
    }

    @Operation(summary = "Найти пропуск по id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск найден",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassDtoResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Пропуск не найден")})
    @GetMapping("/{id}")
    public ResponseEntity<PassDtoResponse> getPass(@PathVariable("id") UUID id) {
        Pass foundPass = service.findPass(id);
        return ResponseEntity.ok(mapper.toPassDTO(foundPass));
    }

    @Operation(summary = "Получить список пропусков конкретного пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуска найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassDtoResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Пропуска не найдены; пользователь не найден")})
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PassDtoResponse>> getPassesByUserId(@PathVariable UUID userId) {
        List<Pass> passes = service.findPassesByUser(userId);
        if (passes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toPassDTO(passes));
    }

    @Operation(summary = "Получить список пропусков на конкретную территорию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуска найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassDtoResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Пропуска не найдены; территория не найдена")})
    @GetMapping("/territory/{territoryId}")
    public ResponseEntity<List<PassDtoResponse>> getPassesByTerritoryId(@PathVariable UUID territoryId) {
        List<Pass> passes = service.findPassesByTerritory(territoryId);
        if (passes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toPassDTO(passes));
    }

    /* UPDATE */
    @Operation(summary = "Изменить существующий пропуск (название, примечание, временной тип, время начала и окончания)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск успешно изменен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassDtoResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидаци полей; пользователь не имеет права " +
                    "создавать пропуск на эту территорию; у пользователя найден накладывающийся пропуск"),
            @ApiResponse(responseCode = "404", description = "Не найден пользователь или территория")})
    @PutMapping
    public ResponseEntity<?> editPass(@RequestBody @Valid PassDtoUpdate passDtoUpdate,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }
        Pass currentPass = service.findPass(passDtoUpdate.getId());
        if (currentPass == null) {
            return ResponseEntity.notFound().build();
        }
        Pass updatedPass = service.updatePass(mapper.toPass(passDtoUpdate));
        return ResponseEntity.ok(mapper.toPassDTO(updatedPass));
    }

    @Operation(summary = "Отменить активный пропуск")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск отменен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassDtoResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Пропуск не является активным"),
            @ApiResponse(responseCode = "404", description = "Пропуск не найден")})
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PassDtoResponse> cancelPass(@PathVariable UUID id) {

        Pass cancelledPass = service.cancelPass(id);
        return ResponseEntity.ok(mapper.toPassDTO(cancelledPass));
    }

    @Operation(summary = "Активировать отмененный пропуск")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск активирован",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassDtoResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Пропуск не является отмененным; время действия пропуска истекло"),
            @ApiResponse(responseCode = "404", description = "Пропуск не найден")})
    @PatchMapping("/{id}/activate")
    public ResponseEntity<PassDtoResponse> activatePass(@PathVariable UUID id) {

        Pass activatedPass = service.activateCancelledPass(id);
        return ResponseEntity.ok(mapper.toPassDTO(activatedPass));
    }

    @Operation(summary = "Отметить выполненным пропуск со статусом Warning (время истекло, последнее пересечение на выезд)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск отмечен выполненным",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassDtoResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Стутус отличен от Warning"),
            @ApiResponse(responseCode = "404", description = "Пропуск не найден")})
    @PatchMapping("/{id}/unwarning")
    public ResponseEntity<PassDtoResponse> unWarningPass(@PathVariable UUID id) {

        Pass completedPass = service.unWarningPass(id);
        return ResponseEntity.ok(mapper.toPassDTO(completedPass));
    }

    @Operation(summary = "Отметить пропуск как избранный")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Отмечен"),
            @ApiResponse(responseCode = "404", description = "Не найден")})
    @PatchMapping("{id}/favorite")
    public ResponseEntity<Void> markFavorite(@PathVariable UUID id) {
        service.markFavorite(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Отметить пропуск как НЕизбранный")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Отмечен"),
            @ApiResponse(responseCode = "404", description = "Не найден")})
    @PatchMapping("{id}/not_favorite")
    public ResponseEntity<Void> unmarkFavorite(@PathVariable UUID id) {
        service.unmarkFavorite(id);
        return ResponseEntity.ok().build();
    }

    /* DELETE */
    @Operation(summary = "Удалить пропуск")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пропуск не найден")})
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deletePass(@PathVariable UUID id) {
        Pass currentPass = service.findPass(id);
        if (currentPass == null) {
            return ResponseEntity.notFound().build();
        }
        service.deletePass(id);
        return ResponseEntity.ok().build();
    }
}
