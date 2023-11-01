package ru.ac.checkpointmanager.model.checkpoints;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.ac.checkpointmanager.model.Territory;

import java.time.LocalDate;
import java.util.UUID;


@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "checkpoints")
public class Checkpoint {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Enumerated(EnumType.STRING)
    private CheckpointType type;

    private String note;

    @CreationTimestamp
    private LocalDate addedAt;

    @ManyToOne
    @JoinColumn(name = "territory_id")
    private Territory territory;
}
