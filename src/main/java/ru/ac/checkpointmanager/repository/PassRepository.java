package ru.ac.checkpointmanager.repository;

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
     * Search for all the passes available in the database that create by specified user
     * @param userId requested user
     * @return list of found passes
     */
    List<Pass> findPassesByUserIdOrderByAddedAtDesc(UUID userId);

    /**
     * Search for all the passes available in the database that relate to the specified territory
     * @param territoryId current territory in the user script
     * @return list of found passes
     */
    List<Pass> findPassesByTerritoryIdOrderByAddedAtDesc(UUID territoryId);

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