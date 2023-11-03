package ru.ac.checkpointmanager.model.passes;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.ac.checkpointmanager.model.Person;

import java.util.Objects;

@Entity
@Setter
@Getter
@DiscriminatorValue("WALK")
public class PassWalk extends Pass {

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "person_id")
    private Person person;

    @Override
    public boolean compareByFields(Pass o) {
        PassWalk other = (PassWalk) o;
        return (super.compareByFields(other) &&
                Objects.equals(this.getPerson().getName(), other.getPerson().getName()));
    }

    @Override
    public String toString() {
        return this.getClass() + "{" +
                super.toString() +
                ", p=" + person.getName() +
                '}';
    }
}