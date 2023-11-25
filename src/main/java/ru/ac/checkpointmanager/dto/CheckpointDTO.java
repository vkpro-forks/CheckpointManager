package ru.ac.checkpointmanager.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.ac.checkpointmanager.model.checkpoints.CheckpointType;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CheckpointDTO {

    private UUID id;

    //@Trimmed
    //кастомная аннотация, проверяет отсутствие пробелов в начале и конце строки
    //реализована классами Trimmed и TrimmedValidator в утилсах
    //потом вместо неё сделал класс StringTrimmer с методом trimThemAll (в сервисах)
    //чтобы не проверять, а просто убирать эти пробелы
    @NotBlank()
    @Size(min = 2, max = 60)
    @Pattern(regexp = "^(?=.*[a-zA-Zа-яА-Я]).*$",
            message = "Name must contain at least one letter")
    private String name;

    @NotNull()
    private CheckpointType type;

    private String note;

    @NotNull()
    private TerritoryDTO territory;
}
