package ru.ac.checkpointmanager.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import ru.ac.checkpointmanager.dto.passes.FilterParams;
import ru.ac.checkpointmanager.exception.pass.InvalidPassStatusException;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassAuto;
import ru.ac.checkpointmanager.model.passes.PassStatus;
import ru.ac.checkpointmanager.model.passes.PassWalk;
import ru.ac.checkpointmanager.specification.model.Car_;
import ru.ac.checkpointmanager.specification.model.PassAuto_;
import ru.ac.checkpointmanager.specification.model.PassWalk_;
import ru.ac.checkpointmanager.specification.model.Pass_;
import ru.ac.checkpointmanager.specification.model.Territory_;
import ru.ac.checkpointmanager.specification.model.User_;
import ru.ac.checkpointmanager.specification.model.Visitor_;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public final class PassSpecification {

    private PassSpecification() {
        throw new AssertionError("No PassSpecification instances for you!");
    }

    public static Specification<Pass> byFilterParams(FilterParams filterParams) {
        log.debug("Filtering parameters are taken: %s".formatted(filterParams));

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String dtype = filterParams.getDtype();
            if (dtype != null && !dtype.isEmpty()) {
                String[] dtypes = filterParams.getDtype().split(",");
                predicates.add(root.get(Pass_.DTYPE).in((Object[]) dtypes));
            }
            String territory = filterParams.getTerritory();
            if (territory != null && !territory.isEmpty()) {
                String[] territories = filterParams.getTerritory().split(",");
                predicates.add(root.get(Pass_.TERRITORY).get(Territory_.NAME).in((Object[]) territories));
            }
            String status = filterParams.getStatus();
            if (status != null && !status.isEmpty()) {
                String[] statusStrings = filterParams.getStatus().split(",");
                List<PassStatus> validStatuses = new ArrayList<>();
                for (String statusString : statusStrings) {
                    try {
                        validStatuses.add(PassStatus.valueOf(statusString));
                    } catch (IllegalArgumentException e) {
                        log.warn("The status {} does not exist, exception - {}", statusString, e.getMessage());
                        throw new InvalidPassStatusException("The status %s does not exist".formatted(statusString));
                    }
                }
                if (!validStatuses.isEmpty()) {
                    predicates.add(root.get(Pass_.STATUS).in(validStatuses));
                }
            }
            Boolean favorite = filterParams.getFavorite();
            if (favorite != null) {
                predicates.add(cb.equal(root.get(Pass_.FAVORITE), favorite));
            }

            if (!query.getResultType().equals(Long.class)) {
                CriteriaBuilder.Case<Integer> statusCase = cb.selectCase();
                Expression<Integer> orderExpression = statusCase
                        .when(cb.equal(root.get(Pass_.STATUS), PassStatus.WARNING), 1)
                        .when(cb.equal(root.get(Pass_.STATUS), PassStatus.ACTIVE), 2)
                        .when(cb.equal(root.get(Pass_.STATUS), PassStatus.DELAYED), 3)
                        .otherwise(4);

                query.orderBy(cb.asc(orderExpression), cb.desc(root.get(Pass_.START_TIME)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Pass> byUserId(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get(Pass_.USER).get(User_.ID), userId);
    }

    public static Specification<Pass> byTerritoryId(UUID territoryId) {
        return (root, query, cb) -> cb.equal(root.get(Pass_.TERRITORY).get(Territory_.ID), territoryId);
    }

    /**
     * Спецификация для создания критерия для поиска пропуска по совпадению номера вложенного автомобиля.
     * Генерирует запрос с LEFT JOIN если в cars поле license_plate строка начинаются с переданного
     * фрагмента строки
     *
     * @param part фрагмент строки для поиска
     * @return {@link Specification<Pass>} спецификация для генерации SQL
     */
    public static Specification<Pass> byCarNumberPart(String part) {
        log.debug("Setting specification for Car for LIKE query with part {}", part);
        return (root, query, criteriaBuilder) -> {
            Root<PassAuto> paRoot = criteriaBuilder.treat(root, PassAuto.class);
            Join<Car, PassAuto> carJoin = paRoot.join(PassAuto_.CAR, JoinType.LEFT);
            return criteriaBuilder.like(carJoin.get(Car_.LICENSE_PLATE), part + "%");
        };
    }

    /**
     * Спецификация для создания критерия для поиска пропуска по совпадению имени вложенного посетителя.
     * Генерирует запрос с LEFT JOIN если в visitors поле full_name строка начинаются с переданного
     * фрагмента строки
     *
     * @param part фрагмент строки для поиска
     * @return {@link Specification<Pass>} спецификация для генерации SQL
     */
    public static Specification<Pass> byVisitorPart(String part) {
        log.debug("Setting specification for Car for LIKE query with part {}", part);
        return (root, query, criteriaBuilder) -> {
            Root<PassWalk> paRoot = criteriaBuilder.treat(root, PassWalk.class);
            Join<Visitor, PassWalk> visitorJoin = paRoot.join(PassWalk_.VISITOR, JoinType.LEFT);
            return criteriaBuilder.like(visitorJoin.get(Visitor_.NAME), part + "%");
        };
    }

}
