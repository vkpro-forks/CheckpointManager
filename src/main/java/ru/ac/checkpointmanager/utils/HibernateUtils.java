package ru.ac.checkpointmanager.utils;

import lombok.experimental.UtilityClass;
import org.hibernate.proxy.HibernateProxy;

@UtilityClass
public class HibernateUtils {

    public static Class<?> getEffectiveClass(Object o) {
        //https://openjdk.org/jeps/394
        return o instanceof HibernateProxy proxy ?
                proxy.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
    }
}
