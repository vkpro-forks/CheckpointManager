package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;

import java.util.List;
import java.util.UUID;

@Repository
public interface TerritoryRepository extends JpaRepository<Territory, UUID> {
    List<Territory> findTerritoriesByNameContainingIgnoreCase(String name);

    @Query("SELECT t.users FROM Territory t WHERE t.id = :territoryId")
    List<User> findUsersByTerritoryId(@Param("territoryId") UUID territoryId);

    @Query(value = "SELECT EXISTS (SELECT FROM user_territory WHERE user_id = :uId AND territory_id = :tId)"
            , nativeQuery = true)
    boolean checkUserTerritoryRelation(@Param("uId") UUID userId, @Param("tId") UUID territoryId);
}
