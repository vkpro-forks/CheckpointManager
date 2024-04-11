package ru.ac.checkpointmanager.specification.model;

import jakarta.persistence.metamodel.StaticMetamodel;
import ru.ac.checkpointmanager.model.User;

@StaticMetamodel(User.class)
public abstract class User_ {

    public static final String ID = "id";

    public static final String FULL_NAME = "fullName";

    public static final String TERRITORIES = "territories";

    public static final String IS_BLOCKED = "isBlocked";

    public static final String ROLE = "role";

    private User_() {
    }

}
