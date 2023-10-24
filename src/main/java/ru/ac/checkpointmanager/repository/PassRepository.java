package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.model.passes.PassStatus;

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
     * Search in the database for active passes that have expired
     * @param time It is supposed to transmit the current date and time
     * @param status It is supposed to transmit the enum value <code>PassStatus.ACTIVE</code>
     * @return list of found passes
     */
    List<Pass> findByEndTimeIsBeforeAndStatusLike(LocalDateTime time, PassStatus status);

    @Transactional
    @Modifying
    @Query(value = "UPDATE passes SET status = 'CANCELLED' WHERE id = :id", nativeQuery = true)
    void cancelById(@Param("id") UUID id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Pass p SET p.status = 'ACTIVE' WHERE p.id = :id")
    void activateById(@Param("id") UUID id);

    @Transactional
    @Modifying
    @Query("UPDATE Pass p SET p.status = 'COMPLETED' WHERE p.id = :id")
    void completedStatusById(@Param("id") UUID id);

    @Query(value = "SELECT EXISTS (SELECT FROM user_territory WHERE user_id = :uId AND territory_id = :tId)"
            , nativeQuery = true)
    boolean checkUserTerritoryRelation(@Param("uId") UUID userId, @Param("tId") UUID territoryId);
}