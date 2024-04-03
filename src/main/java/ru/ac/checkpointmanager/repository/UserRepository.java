package ru.ac.checkpointmanager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Collection<User> findUserByFullNameContainingIgnoreCase(String name);

    Optional<User> findByEmail(String email);

    @Transactional
    @Modifying
    @Query(value = "UPDATE users u SET is_blocked = true WHERE u.id = :id", nativeQuery = true)
    void blockById(@Param("id") UUID id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE users u SET is_blocked = false WHERE u.id = :id", nativeQuery = true)
    void unblockById(@Param("id") UUID id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.territories WHERE u.id= :userId")
    Optional<User> findUserWithTerritoriesById(@Param("userId") UUID userId);

    @Query("SELECT new ru.ac.checkpointmanager.model.User(u.id, a.id) FROM User u LEFT JOIN u.avatar a WHERE  u.id= :userId")
    Optional<User> findUserWithAvatarIdById(@Param("userId") UUID uuid);

    @Modifying
    @Query("update User u set u.avatar = :avatar where u.id = :userId")
    void setAvatarForUser(@Param("avatar") Avatar avatar, @Param("userId") UUID userId);

    @Query(value = "SELECT avatar_id FROM users WHERE id = :userId", nativeQuery = true)
    UUID findAvatarIdByUserId(@Param("userId") UUID userId);

    @Query(value = "SELECT u.* FROM users u JOIN passes p on u.id = p.user_id WHERE p.id = :passId", nativeQuery = true)
    User findByPassId(@Param("passId") UUID passId);

    @Query("SELECT u FROM User u JOIN FETCH u.territories t WHERE t.id= :territoryId " +
            "ORDER BY CASE WHEN u.role = 'ADMIN' THEN 1 WHEN u.role = 'MANAGER' THEN 2 " +
            "WHEN u.role = 'SECURITY' THEN 3 ELSE 4 END, u.fullName")
    Page<User> findUsersByTerritoryId(@Param("territoryId") UUID territoryId, Pageable pageable);

    @Query("SELECT u FROM User u " +
            "ORDER BY CASE WHEN u.role = 'ADMIN' THEN 1 WHEN u.role = 'MANAGER' THEN 2 " +
            "WHEN u.role = 'SECURITY' THEN 3 ELSE 4 END, u.fullName")
    @NonNull
    Page<User> findAll(@NonNull Pageable pageable);

    @Query("SELECT u2 FROM User u1 JOIN u1.territories t JOIN t.users u2 WHERE u1.id = :userId AND u2.id != :userId " +
            "ORDER BY CASE WHEN u2.role = 'ADMIN' THEN 1 WHEN u2.role = 'MANAGER' THEN 2 " +
            "WHEN u2.role = 'SECURITY' THEN 3 ELSE 4 END, u2.fullName")
    Page<User> findTerritoriesAssociatedUsers(@Param("userId") UUID userId, Specification<User> spec, Pageable pageable);

    boolean existsByEmail(String email);

}
