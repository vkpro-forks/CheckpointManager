package ru.ac.checkpointmanager.specification.model;

import jakarta.persistence.metamodel.StaticMetamodel;
import ru.ac.checkpointmanager.model.passes.Pass;

@StaticMetamodel(Pass.class)
public abstract class Pass_ {

    public static final String DTYPE = "dtype";

    public static final String DTYPE_AUTO = "AUTO";

    public static final String DTYPE_WALK = "WALK";

    public static final String TERRITORY = "territory";

    public static final String USER = "user";

    public static final String START_TIME = "startTime";

    public static final String END_TIME = "endTime";

    public static final String STATUS = "status";

    public static final String FAVORITE = "favorite";

    private Pass_() {
    }

}
