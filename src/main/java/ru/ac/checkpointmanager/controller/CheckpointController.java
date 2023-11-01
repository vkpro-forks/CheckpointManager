package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.CheckpointDTO;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.service.checkpoints.CheckpointService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import jakarta.validation.*;
import ru.ac.checkpointmanager.utils.Mapper;

import java.util.List;
import java.util.UUID;

import static ru.ac.checkpointmanager.utils.Mapper.*;

@RestController
@RequestMapping("chpman/checkpoint")
@RequiredArgsConstructor
@Tag(name = "Checkpoint (КПП)", description = "Администрирование списка КПП для обслуживаемых территорий")
@ApiResponses(value = {@ApiResponse(responseCode = "500",
        description = "Произошла ошибка, не зависящая от вызывающей стороны")})
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
public class  CheckpointController {

    private final CheckpointService service;
    private final Mapper mapper;

    /* CREATE */
    @Operation(summary = "Добавить новый КПП")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "КПП успешно добавлен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckpointDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидаци полей; не найдена указанная территория")})
    @PostMapping
    public ResponseEntity<?> addCheckpoint(@RequestBody @Valid CheckpointDTO checkpointDTO,
                                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Checkpoint newCheckpoint = service.addCheckpoint(mapper.toCheckpoint(checkpointDTO));
        return ResponseEntity.ok(mapper.toCheckpointDTO(newCheckpoint));
    }

    /* READ */
    @Operation(summary = "Найти КПП по id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "КПП найден",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckpointDTO.class))}),
            @ApiResponse(responseCode = "404", description = "КПП не найден")})
    @GetMapping("/{id}")
    public ResponseEntity<CheckpointDTO> getCheckpoint(@PathVariable("id") UUID id) {
        Checkpoint foundCheckpoint = service.findCheckpointById(id);
        if (foundCheckpoint == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toCheckpointDTO(foundCheckpoint));
    }

    @Operation(summary = "Найти список КПП по названию")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "КПП найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = CheckpointDTO.class)))),
            @ApiResponse(responseCode = "404", description = "КПП не найдены")})
    @GetMapping("/name")
    public ResponseEntity<List<CheckpointDTO>> getCheckpointsByName(@Parameter(description = "Часть названия")
                                                                    @RequestParam String name) {
        List<Checkpoint> checkpoints = service.findCheckpointsByName(name);
        if (checkpoints.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toCheckpointsDTO(checkpoints));
    }

    @Operation(summary = "Получить список всех КПП")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "КПП найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = CheckpointDTO.class)))),
            @ApiResponse(responseCode = "404", description = "КПП не найдены")})
    @GetMapping
    public ResponseEntity<List<CheckpointDTO>> getCheckpoints() {
        List<Checkpoint> checkpoints = service.findAllCheckpoints();
        if (checkpoints.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toCheckpointsDTO(checkpoints));
    }

    @Operation(summary = "Получить список КПП, привязанных к указанной территории")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "КПП найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = CheckpointDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Не найдена указанная территория"),
            @ApiResponse(responseCode = "404", description = "КПП не найдены")})
    @GetMapping("/territory/{territoryId}")
    public ResponseEntity<List<CheckpointDTO>> getCheckpointsByTerritoryId(@Parameter(description = "ID территории")
                                                                               @PathVariable UUID territoryId) {
        List<Checkpoint> checkpoints = service.findCheckpointsByTerritoryId(territoryId);
        if (checkpoints.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toCheckpointsDTO(checkpoints));
    }

    /* UPDATE */
    @Operation(summary = "Изменить существующий КПП")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "КПП успешно изменем",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckpointDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидаци полей; не найдена указанная территория"),
            @ApiResponse(responseCode = "404", description = "КПП не найден")})
    @PutMapping
    public ResponseEntity<?> editCheckpoint(@RequestBody @Valid CheckpointDTO checkpointDTO,
                                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Checkpoint updatedCheckpoint = service.updateCheckpoint(mapper.toCheckpoint(checkpointDTO));
        return ResponseEntity.ok(mapper.toCheckpointDTO(updatedCheckpoint));
    }

    /* DELETE */
    @Operation(summary = "Удалить КПП")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "КПП успешно удален"),
            @ApiResponse(responseCode = "404", description = "КПП не найден")})
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteCheckpoint(@PathVariable UUID id) {

        service.deleteCheckpointById(id);
        return ResponseEntity.ok().build();
    }
}
