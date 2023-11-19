package ru.ac.checkpointmanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.model.passes.PassWalk;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "visitors")
@NoArgsConstructor
@AllArgsConstructor
public class Visitor {


    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name")
    private String name;

    @Column(name = "visitor_phone")
    private String phone;

    @OneToMany(mappedBy = "visitor")
    private List<PassWalk> passes;

    private String note;

}
