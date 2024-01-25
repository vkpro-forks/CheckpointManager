package ru.ac.checkpointmanager.specification.model;

import jakarta.persistence.metamodel.StaticMetamodel;
import ru.ac.checkpointmanager.model.Visitor;

@StaticMetamodel(Visitor.class)
public abstract class Visitor_ {

    public static final String NAME = "name";

    private Visitor_() {
    }

}
