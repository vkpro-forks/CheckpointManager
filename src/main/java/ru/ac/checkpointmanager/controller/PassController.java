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
import ru.ac.checkpointmanager.dto.*;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.service.passes.PassService;
import ru.ac.checkpointmanager.utils.ErrorUtils;
import ru.ac.checkpointmanager.utils.Mapper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("chpman/pass")
@RequiredArgsConstructor
@Tag(name = "Pass (пропуска)", description = "Операции с пропусками для машин и людей")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = "UNAUTHORIZED: пользователь не авторизован"),
        @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса")})
@SecurityRequirement(name = "bearerAuth")
public class PassController {

    private final PassService service;
    private final Mapper mapper;

    /* CREATE */
    @Operation(summary = "Добавить новый пропуск",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск успешно добавлен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassDTOout.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей; пользователь не имеет права " +
                    "создавать пропуск на эту территорию; у пользователя найден накладывающийся пропуск"),
            @ApiResponse(responseCode = "404", description = "Не найден пользователь или территория")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @PostMapping
    public ResponseEntity<?> addPass(@RequestBody @Valid PassDTOin passDTOin,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Pass newPass = service.addPass(mapper.toPass(passDTOin));
        return ResponseEntity.ok(mapper.toPassDTO(newPass));
    }

    /* READ */
    @Operation(summary = "Получить список всех пропусков",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуска найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassDTOout.class)))),
            @ApiResponse(responseCode = "404", description = "Пропуска не найдены")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<List<PassDTOout>> getPasses() {
        List<Pass> passes = service.findPasses();
        if (passes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toPassDTO(passes));
    }

    @Operation(summary = "Найти пропуск по id",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск найден",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassDTOout.class))}),
            @ApiResponse(responseCode = "404", description = "Пропуск не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @GetMapping("/{id}")
    public ResponseEntity<PassDTOout> getPass(@PathVariable("id") UUID id) {
        Pass foundPass = service.findPass(id);
        return ResponseEntity.ok(mapper.toPassDTO(foundPass));
    }

    @Operation(summary = "Получить список пропусков конкретного пользователя",
            description = "Доступ: ADMIN, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуска найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassDTOout.class)))),
            @ApiResponse(responseCode = "404", description = "Пропуска не найдены; пользователь не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PassDTOout>> getPassesByUserId(@PathVariable UUID userId) {
        List<Pass> passes = service.findPassesByUser(userId);
        if (passes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toPassDTO(passes));
    }

    @Operation(summary = "Получить список пропусков на конкретную территорию",
            description = "Доступ: ADMIN, MANAGER, SECURITY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуска найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassDTOout.class)))),
            @ApiResponse(responseCode = "404", description = "Пропуска не найдены; территория не найдена")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/territory/{territoryId}")
    public ResponseEntity<List<PassDTOout>> getPassesByTerritoryId(@PathVariable UUID territoryId) {
        List<Pass> passes = service.findPassesByTerritory(territoryId);
        if (passes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toPassDTO(passes));
    }

    /* UPDATE */
    @Operation(summary = "Изменить существующий пропуск (название, примечание, временной тип, время начала и окончания)",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск успешно изменен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassDTOout.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидаци полей; пользователь не имеет права " +
                    "создавать пропуск на эту территорию; у пользователя найден накладывающийся пропуск"),
            @ApiResponse(responseCode = "404", description = "Не найден пользователь или территория")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @PutMapping
    public ResponseEntity<?> editPass(@RequestBody @Valid PassDTOin passDTOin,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }
        Pass currentPass = service.findPass(passDTOin.getId());
        if (currentPass == null) {
            return ResponseEntity.notFound().build();
        }
        Pass updatedPass = service.updatePass(mapper.toPass(passDTOin));
        return ResponseEntity.ok(mapper.toPassDTO(updatedPass));
    }

    @Operation(summary = "Отменить активный пропуск",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск отменен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassDTOout.class))}),
            @ApiResponse(responseCode = "400", description = "Пропуск не является активным"),
            @ApiResponse(responseCode = "404", description = "Пропуск не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PassDTOout> cancelPass(@PathVariable UUID id) {

        Pass cancelledPass = service.cancelPass(id);
        return ResponseEntity.ok(mapper.toPassDTO(cancelledPass));
    }

    @Operation(summary = "Активировать отмененный пропуск",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск активирован",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassDTOout.class))}),
            @ApiResponse(responseCode = "400", description = "Пропуск не является отмененным; время действия пропуска истекло"),
            @ApiResponse(responseCode = "404", description = "Пропуск не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<PassDTOout> activatePass(@PathVariable UUID id) {

        Pass activatedPass = service.activateCancelledPass(id);
        return ResponseEntity.ok(mapper.toPassDTO(activatedPass));
    }

    @Operation(summary = "Отметить выполненным пропуск со статусом Warning (время истекло, последнее пересечение на выезд)",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск отмечен выполненным",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassDTOout.class))}),
            @ApiResponse(responseCode = "400", description = "Стутус отличен от Warning"),
            @ApiResponse(responseCode = "404", description = "Пропуск не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @PatchMapping("/{id}/unwarning")
    public ResponseEntity<PassDTOout> unWarningPass(@PathVariable UUID id) {

        Pass completedPass = service.unWarningPass(id);
        return ResponseEntity.ok(mapper.toPassDTO(completedPass));
    }

    /* DELETE */
    @Operation(summary = "Удалить пропуск",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пропуск не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
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
