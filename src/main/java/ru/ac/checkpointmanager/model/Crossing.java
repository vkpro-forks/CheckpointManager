package ru.ac.checkpointmanager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.model.passes.Pass;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "crossings")
public class Crossing {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "pass_id", referencedColumnName = "id")
    private Pass pass;

    @ManyToOne
    @JoinColumn(name = "checkpoint_id", referencedColumnName = "id")
    private Checkpoint checkpoint;

    @Column(name = "performed_at")
    private ZonedDateTime performedAt;

    @Column(name = "local_date_time")
    @CreationTimestamp(source = SourceType.VM)
    private LocalDateTime localDateTime;

    @Enumerated(EnumType.STRING)
    private Direction direction;

    @Override
    public String toString() {
        return "Crossing{" + id +
                ", pass=" + pass.getId() +
                ", checkpoint=" + checkpoint.getId() +
                ", time=" + localDateTime +
                ", dir=" + direction +
                '}';
    }
}
