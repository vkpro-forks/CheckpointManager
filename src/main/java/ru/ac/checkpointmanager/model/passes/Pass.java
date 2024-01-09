package ru.ac.checkpointmanager.model.passes;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.enums.Direction;

import java.time.LocalDateTime;
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
    @GeneratedValue
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String comment;

    @Enumerated(EnumType.STRING)
    private PassStatus status;

    @Enumerated(EnumType.STRING)
    private PassTimeType timeType;

    @ManyToOne
    @JoinColumn(name = "territory_id")
    private Territory territory;
    //#FIXME удалить crossing is зависимостей пасса если есть
    @CreationTimestamp(source = SourceType.VM)
    private LocalDateTime addedAt;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(insertable=false, updatable=false)
    private String dtype;

    private Boolean favorite = false;

    @Enumerated(EnumType.STRING)
    private Direction expectedDirection = Direction.IN;

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
        return "Pass{" + id +
                ", user=" + user.getId() +
                ", comment=" + comment +
                ", status=" + status +
                ", typeTime=" + timeType +
                ", territory=" + territory.getId() +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", dtype=" + dtype +
                ", favorite=" + favorite +
                ", expDir=" + expectedDirection +
                '}';
    }
}