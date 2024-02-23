package ru.ac.checkpointmanager.model.checkpoints;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
