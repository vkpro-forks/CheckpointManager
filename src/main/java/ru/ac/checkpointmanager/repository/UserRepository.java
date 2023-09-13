package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Collection<User> findUserByFullNameContainingIgnoreCase(String name);

    User findUserByEmail(String email);

    @Transactional
    @Modifying
    @Query(value = "UPDATE users u SET is_blocked = true WHERE u.id = :id", nativeQuery = true)
    void blockById(@Param("id") UUID id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE users u SET is_blocked = false WHERE u.id = :id", nativeQuery = true)
    void unblockById(@Param("id") UUID id);

    @Query("SELECT u.territories FROM User u WHERE u.id = :userId")
    List<Territory> findTerritoriesByUserId(@Param("userId") UUID userId);
}
