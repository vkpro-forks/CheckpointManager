package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.model.Pass;
import ru.ac.checkpointmanager.model.enums.PassStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface PassRepository extends JpaRepository<Pass, UUID> {

    List<Pass> findPassesByUserIdOrderByAddedAtDesc(UUID userId);
    List<Pass> findPassesByTerritoryIdOrderByAddedAtDesc(UUID userId);

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
}