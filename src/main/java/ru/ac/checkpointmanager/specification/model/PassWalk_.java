package ru.ac.checkpointmanager.specification.model;

import jakarta.persistence.metamodel.StaticMetamodel;
import ru.ac.checkpointmanager.model.passes.PassWalk;

@StaticMetamodel(PassWalk.class)
public abstract class PassWalk_ {

    public static final String VISITOR = "visitor";

    private PassWalk_() {
    }

}
