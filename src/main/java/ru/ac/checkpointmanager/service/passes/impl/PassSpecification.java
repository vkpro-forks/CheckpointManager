package ru.ac.checkpointmanager.service.passes.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import ru.ac.checkpointmanager.dto.passes.FilterParams;
import ru.ac.checkpointmanager.exception.pass.InvalidPassStatusException;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassStatus;

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

        String dtype = filterParams.getDtype();
        String territory = filterParams.getTerritory();
        String status = filterParams.getStatus();
        Boolean favorite = filterParams.getFavorite();

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (dtype != null && !dtype.isEmpty()) {
                String[] dtypes = filterParams.getDtype().split(",");
                predicates.add(root.get("dtype").in((Object[]) dtypes));
            }

            if (territory != null && !territory.isEmpty()) {
                String[] territories = filterParams.getTerritory().split(",");
                predicates.add(root.get("territory").get("name").in((Object[]) territories));
            }

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
                    predicates.add(root.get("status").in(validStatuses));
                }
            }

            if (favorite != null) {
                predicates.add(cb.equal(root.get("favorite"), favorite));
            }

            if (!query.getResultType().equals(Long.class)) {
                CriteriaBuilder.Case<Integer> statusCase = cb.selectCase();
                Expression<Integer> orderExpression = statusCase
                        .when(cb.equal(root.get("status"), PassStatus.WARNING), 1)
                        .when(cb.equal(root.get("status"), PassStatus.ACTIVE), 2)
                        .when(cb.equal(root.get("status"), PassStatus.DELAYED), 3)
                        .otherwise(4);

                query.orderBy(cb.asc(orderExpression), cb.desc(root.get("startTime")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Pass> byUserId(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Pass> byTerritoryId(UUID territoryId) {
        return (root, query, cb) -> cb.equal(root.get("territory").get("id"), territoryId);
    }

    public static Specification<Pass> byCarNumberOrVisitorNamePart(String part) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (root.get("car") != null) {
                predicates.add(criteriaBuilder.like(root.get("car").get("licencePlate"), "%" + part));
            }
            if (root.get("visitor") != null) {
                predicates.add(criteriaBuilder.like(root.get("visitor").get("name"), "%" + part));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}