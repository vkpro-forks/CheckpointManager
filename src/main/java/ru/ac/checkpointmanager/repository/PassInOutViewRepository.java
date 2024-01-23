package ru.ac.checkpointmanager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.ac.checkpointmanager.projection.PassInOutViewProjection;

import java.util.UUID;

public interface PassInOutViewRepository extends JpaRepository<PassInOutViewProjection, UUID> {

    @Query(value = "SELECT * FROM pass_in_out_view p WHERE p.user_id = :userId ORDER BY in_time DESC"
            , nativeQuery = true)
    Page<PassInOutViewProjection> findEventsByUser(UUID userId, Pageable pageable);

    @Query(value = "SELECT * FROM pass_in_out_view p WHERE p.territory_id = :terId ORDER BY in_time DESC"
            , nativeQuery = true)
    Page<PassInOutViewProjection> findEventsByTerritory(UUID terId, Pageable pageable);

}
