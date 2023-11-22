package ru.ac.checkpointmanager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ac.checkpointmanager.model.passes.Pass;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository class for <code>Pass</code> domain objects
 *
 * @author Dmitry Ldv236
 */
@Repository
public interface PassRepository extends JpaRepository<Pass, UUID> {

    /**
     * A SQL fragment defining the custom sort logic for Pass entities.
     * This logic sorts entities primarily based on their status in the specific order:
     * WARNING, ACTIVE, DELAYED, followed by any other statuses. After sorting by status,
     * entities are further sorted by their start time in descending order.
     */
    String SORT_LOGIC = "ORDER BY CASE " +
            "p.status WHEN 'WARNING' THEN 1 WHEN 'ACTIVE' THEN 2 WHEN 'DELAYED' THEN 3 ELSE 4 END, " +
            "p.start_time DESC";

    /**
     * Retrieves a page of Pass objects sorted by a custom logic
     * @param pageable the {@link Pageable} object containing pagination information.
     * @return a {@link Page} of Pass objects sorted as per the defined logic.
     */
    @Query(value = "SELECT * FROM passes p " + SORT_LOGIC, nativeQuery = true)
    Page<Pass> findAll(Pageable pageable);

    /**
     * Retrieves a page of Pass objects for a specific user for check overlapped passes during adding new pass
     * @param userId requested user
     * @return list of found passes
     */
    List<Pass> findAllPassesByUserId(UUID userId);

    /**
     * Retrieves a page of Pass objects for a specific user, sorted by a custom logic
     * @param userId the UUID of the user.
     * @param pageable the {@link Pageable} object containing pagination information.
     * @return a {@link Page} of Pass objects related to the specified user and sorted as per the defined logic.
     */
    @Query(value = "SELECT * FROM passes p WHERE p.user_id = :userId " + SORT_LOGIC, nativeQuery = true)
    Page<Pass> findPassesByUserId(UUID userId, Pageable pageable);

    /**
     * Retrieves a page of Pass objects for a specific territory, sorted by a custom logic
     * @param territoryId the UUID of the territory.
     * @param pageable the {@link Pageable} object containing pagination information.
     * @return a {@link Page} of Pass objects related to the specified territory and sorted as per the defined logic.
     */
    @Query(value = "SELECT * FROM passes p WHERE p.territory_id = :territoryId " + SORT_LOGIC, nativeQuery = true)
    Page<Pass> findPassesByTerritoryId(UUID territoryId, Pageable pageable);

    /**
     * Search passes by status and reached start- or end-time
     * @param status It is supposed to transmit the value PassStatus.?.toString()
     * @param timeColumn string value of column name to compare time
     * @param time date and time to compare with timeColumn
     * @return list of found passes
     */
    @Query(value = "SELECT * FROM passes WHERE status = :status AND " +
            "CASE WHEN :column = 'startTime' THEN start_time " +
            "WHEN :column = 'endTime' THEN end_time " +
            "END < :time" , nativeQuery = true)
    List<Pass> findPassesByStatusAndTimeBefore(@Param("status") String status
            , @Param("column") String timeColumn, @Param("time") LocalDateTime time);

    /**
     * checks the connection between the user and the territories
     * (the user's permission to create a pass to this territory)
     * @param userId id проверяемого пользователя
     * @param territoryId id проверяемой территории
     * @return bool result of check
     */
    @Query(value = "SELECT EXISTS (SELECT FROM user_territory WHERE user_id = :uId AND territory_id = :tId)"
            , nativeQuery = true)
    boolean checkUserTerritoryRelation(@Param("uId") UUID userId, @Param("tId") UUID territoryId);
}