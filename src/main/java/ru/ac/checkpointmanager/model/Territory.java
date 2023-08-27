package ru.ac.checkpointmanager.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;


@Entity
@Data
@NoArgsConstructor
@Table(name = "territories")
public class Territory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotEmpty()
    @Size(min = 2, max = 60)
    private String name;

    private String note;

    private LocalDate addedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "territory")
    private Set<Checkpoint> checkpoints;
}
