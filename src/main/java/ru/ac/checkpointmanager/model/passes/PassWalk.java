package ru.ac.checkpointmanager.model.passes;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.ac.checkpointmanager.model.Person;

@Entity
@Setter
@Getter
@DiscriminatorValue("WALK")
public class PassWalk extends Pass {

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "person_id")
    private Person person;
}