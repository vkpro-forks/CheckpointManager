package ru.ac.checkpointmanager.model;

import jakarta.persistence.*;
import javax.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "checkpoints")
public class Checkpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty()
    @Size(min = 2, max = 60, message = "Name should be between 2 and 60 symbols")
    private String name;

    @Enumerated(EnumType.STRING)
    private CheckpointType type;

    private String note;

    private LocalDateTime addedAt;
}
