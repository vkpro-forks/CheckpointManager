package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ac.checkpointmanager.model.Territory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TerritoryRepository extends JpaRepository<Territory, UUID> {
    List<Territory> findTerritoriesByNameContainingIgnoreCase(String name);

    @Query(value = "SELECT EXISTS (SELECT FROM user_territory WHERE user_id = :uId AND territory_id = :tId)"
            , nativeQuery = true)
    boolean checkUserTerritoryRelation(@Param("uId") UUID userId, @Param("tId") UUID territoryId);

    @Query(value = "SELECT t.* FROM territories t JOIN passes p on t.id = p.territory_id WHERE p.id = :passId", nativeQuery = true)
    Territory findByPassId(@Param("passId") UUID passId);

    @Query(value = "SELECT t FROM Territory t LEFT JOIN FETCH t.avatar WHERE t.id = :territoryId")
    Optional<Territory> findTerritoryByIdWithAvatar(@Param("territoryId") UUID territoryId);

    @Query("SELECT t FROM User u1 JOIN u1.territories t JOIN User u2 ON t MEMBER OF u2.territories WHERE u1.id = :userId1 AND u2.id = :userId2")
    List<Territory> findCommonTerritories(@Param("userId1") UUID userId1, @Param("userId2") UUID userId2);
}
