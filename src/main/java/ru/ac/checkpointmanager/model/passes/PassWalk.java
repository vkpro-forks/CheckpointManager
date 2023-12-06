package ru.ac.checkpointmanager.model.passes;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.ac.checkpointmanager.model.Visitor;

import java.util.Objects;

@Entity
@Setter
@Getter
@DiscriminatorValue("WALK")
public class PassWalk extends Pass {

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "visitor_id")
    private Visitor visitor;

    @Override
    public boolean compareByFields(Pass o) {
        PassWalk other = (PassWalk) o;
        return (super.compareByFields(other) &&
                Objects.equals(this.getVisitor().getName(), other.getVisitor().getName()));
    }

    @Override
    public void setAttachedEntity(Pass pass) {
        this.visitor = ((PassWalk) pass).getVisitor();
    }

    @Override
    public String toString() {
        return this.getClass() + "{" +
                super.toString() +
                ", p=" + visitor.getName() +
                '}';
    }
}