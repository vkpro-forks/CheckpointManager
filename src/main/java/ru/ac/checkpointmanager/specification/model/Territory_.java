package ru.ac.checkpointmanager.specification.model;

import jakarta.persistence.metamodel.StaticMetamodel;
import ru.ac.checkpointmanager.model.Territory;

@StaticMetamodel(Territory.class)
public abstract class Territory_ {

    public static final String ID = "id";

    public static final String NAME = "name";

    private Territory_() {
    }

}
