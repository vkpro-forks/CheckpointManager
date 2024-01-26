package ru.ac.checkpointmanager.specification.model;

import jakarta.persistence.metamodel.StaticMetamodel;
import ru.ac.checkpointmanager.model.User;

@StaticMetamodel(User.class)
public abstract class User_ {

    public static final String ID = "id";

    private User_() {
    }

}
