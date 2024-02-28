package ru.ac.checkpointmanager.specification.model;

import jakarta.persistence.metamodel.StaticMetamodel;
import ru.ac.checkpointmanager.model.passes.PassAuto;

@StaticMetamodel(PassAuto.class)
public abstract class PassAuto_ {

    public static final String CAR = "car";

    public static final String DTYPE = "AUTO";

    private PassAuto_() {
    }

}
