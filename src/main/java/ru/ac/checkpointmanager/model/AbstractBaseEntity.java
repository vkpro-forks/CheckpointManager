package ru.ac.checkpointmanager.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.ac.checkpointmanager.utils.HibernateUtils;

import java.util.UUID;

@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
public abstract class AbstractBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID id;

    //  https://jpa-buddy.com/blog/hopefully-the-final-article-about-equals-and-hashcode-for-jpa-entities-with-db-generated-ids/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || HibernateUtils.getEffectiveClass(this) != HibernateUtils.getEffectiveClass(o)) return false;
        return getId() != null && getId().equals(((AbstractBaseEntity) o).getId());
    }

    @Override
    public final int hashCode() {
        return HibernateUtils.getEffectiveClass(this).hashCode();
    }
}
