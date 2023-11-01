package ru.ac.checkpointmanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.engine.internal.Cascade;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassWalk;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "persons")
@NoArgsConstructor
@AllArgsConstructor
public class Person {


    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name")
    private String name;

    @Column(name = "person_phone")
    private String phone;

    @OneToMany(mappedBy = "person")
    private List<PassWalk> passes;

    private String note;

}
