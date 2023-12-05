package ru.ac.checkpointmanager.model.passes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "passes")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RequiredArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="dtype", discriminatorType = DiscriminatorType.STRING)
public abstract class Pass {

    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String comment;

    @Enumerated(EnumType.STRING)
    private PassStatus status;

    @Enumerated(EnumType.STRING)
    private PassTypeTime typeTime;

    @ManyToOne
    @JoinColumn(name = "territory_id")
    private Territory territory;

    @CreationTimestamp(source = SourceType.VM)
    private LocalDateTime addedAt;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(insertable=false, updatable=false)
    private String dtype;

    @JsonIgnore
    @OneToMany(mappedBy = "pass")
    private List<Crossing> crossings;

    Boolean favorite = false;

    public boolean compareByFields(Pass other) {
        return  (Objects.equals(this.getUser(), other.getUser()) &&
                !Objects.equals(this.getId(), other.getId()) &&
                Objects.equals(this.getTerritory(), other.getTerritory()) &&
                this.getEndTime().isAfter(other.getStartTime()) &&
                this.getStartTime().isBefore(other.getEndTime()));
    }

    public abstract void setAttachedEntity(Pass pass);

    @Override
    public String toString() {
        return  "id=" + id +
                ", u=" + user.getId() +
                ", " + status +
                ", " + typeTime +
                ", s=" + startTime +
                ", e=" + endTime +
                ", t=" + territory.getId() +
                ", dtype=" + dtype;
    }
}