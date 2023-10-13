package ru.ac.checkpointmanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import ru.ac.checkpointmanager.model.enums.Direction;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "crossings")
public class Crossing {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "pass_id", referencedColumnName = "id")
    private Pass pass;

    @ManyToOne
    @JoinColumn(name = "checkpoint_id", referencedColumnName = "id")
    private Checkpoint checkpoint;

    @Column(name = "local_date_time")
    @CreationTimestamp(source = SourceType.VM)
    private LocalDateTime localDateTime;

    @Enumerated(EnumType.STRING)
    private Direction direction;

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Crossing crossing = (Crossing) o;
        return Objects.equals(id, crossing.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
