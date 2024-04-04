package ru.ac.checkpointmanager.specification;

import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import ru.ac.checkpointmanager.dto.user.UserFilterParams;
import ru.ac.checkpointmanager.exception.InvalidUserRoleException;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.specification.model.Territory_;
import ru.ac.checkpointmanager.specification.model.User_;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class UserSpecification {

    private UserSpecification() {
        throw new AssertionError("No PassSpecification instances for you!");
    }

    public static Specification<User> byFilterParams(UserFilterParams filterParams) {
        log.debug("Filtering parameters are taken: %s".formatted(filterParams));

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String territory = filterParams.getTerritories();
            if (territory != null && !territory.isEmpty()) {
                String[] territories = filterParams.getTerritories().split(",");
                predicates.add(root.get(User_.TERRITORIES).get(Territory_.NAME).in((Object[]) territories));
            }

            Boolean isBlocked = filterParams.getIsBlocked();
            if (isBlocked != null) {
                predicates.add(root.get(User_.IS_BLOCKED).in(isBlocked));
            }

            String role = filterParams.getRole();
            if (role != null && !role.isEmpty()) {
                String[] roleStrings = filterParams.getRole().split(",");
                List<Role> validRoles = new ArrayList<>();
                for (String roleString : roleStrings) {
                    try {
                        validRoles.add(Role.valueOf(roleString));
                    } catch (IllegalArgumentException e) {
                        log.warn("The role {} does not exist, exception - {}", roleString, e.getMessage());
                        throw new InvalidUserRoleException("The role %s does not exist".formatted(roleString));
                    }
                }
                if (!validRoles.isEmpty()) {
                    predicates.add(root.get(User_.ROLE).in(validRoles));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Генерация спецификации для фильтрации записей по фрагменту переданной строки
     * @param part фрагмент строки поиска
     * @return {@link Specification<User>} спецификация для генерации SQL
     */
    public static Specification<User> byFullNamePart(String part) {
        log.debug("Setting specification for User for LIKE query with part {}", part);
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get(User_.FULL_NAME)), "%" + part.toLowerCase() + "%");
        };
    }
}
