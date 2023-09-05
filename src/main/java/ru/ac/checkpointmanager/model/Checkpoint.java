package ru.ac.checkpointmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import ru.ac.checkpointmanager.model.enums.CheckpointType;

import java.time.LocalDate;
import java.util.UUID;


@Entity
@Data
@NoArgsConstructor
@Table(name = "checkpoints")
public class Checkpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotEmpty()
    @Size(min = 2, max = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    private CheckpointType type;

    private String note;

    private LocalDate addedAt;

    @ManyToOne
    @JoinColumn(name = "territory_id")
    private Territory territory;
}
