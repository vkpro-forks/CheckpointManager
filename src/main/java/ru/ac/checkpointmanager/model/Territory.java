package ru.ac.checkpointmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.passes.Pass;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Entity
@Data
@Table(name = "territories")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Territory {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String note;

    @CreationTimestamp
    private LocalDate addedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "territory")
    private Set<Checkpoint> checkpoints;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_territory",
            joinColumns = @JoinColumn(name = "territory_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> users;

    @JsonIgnore
    @OneToMany(mappedBy = "territory", fetch = FetchType.LAZY)
    private List<Pass> pass;

    @Override
    public String toString() {
        return "Territory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", note='" + note + '\'' +
                '}';
    }
}
