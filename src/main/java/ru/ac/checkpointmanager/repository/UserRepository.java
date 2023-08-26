package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.model.User;

import java.util.Collection;
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
}
