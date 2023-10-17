package ru.ac.checkpointmanager.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Data
@Entity
@Table(name = "persons")
@NoArgsConstructor
public class Person {


    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "fullName")
    private String name;

    @Column(name = "person_phone")
    private String phone;

    @OneToMany(mappedBy = "person")
    private List<Pass> passes;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person p = (Person) o;
        return Objects.equals(id, p.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
