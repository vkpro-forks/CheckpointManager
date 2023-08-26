package ru.ac.checkpointmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.model.enums.CheckpointType;

import java.time.LocalDateTime;


@Entity
@Data
@NoArgsConstructor
@Table(name = "checkpoints")
public class Checkpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty()
    @Size(min = 2, max = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    private CheckpointType type;

    private String note;

    private LocalDateTime addedAt;
}
