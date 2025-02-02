package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ac.checkpointmanager.model.avatar.Avatar;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvatarRepository extends JpaRepository<Avatar, UUID> {

    Optional<Avatar> findByUserId(UUID userId);
    Optional<Avatar> findByTerritoryId(UUID territoryId);

}
