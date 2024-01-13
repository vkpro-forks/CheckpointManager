package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository //FIXME кстати можно не ставить спринг все что экстендится от JPA репозитори считает репо
public interface UserRepository extends JpaRepository<User, UUID> {
    Collection<User> findUserByFullNameContainingIgnoreCase(String name);

    Optional<User> findByEmail(String email);

    @Transactional // FIXME оно и без это транзакции будет в транзакции (просто для информации, тут эта аннотация лишняя)
    @Modifying
    @Query(value = "UPDATE users u SET is_blocked = true WHERE u.id = :id", nativeQuery = true)
    void blockById(@Param("id") UUID id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE users u SET is_blocked = false WHERE u.id = :id", nativeQuery = true)
    void unblockById(@Param("id") UUID id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.territories WHERE u.id= :userId")
    Optional<User> findUserWithTerritoriesById(@Param("userId") UUID userId);

    @Modifying
    @Query("update User u set u.avatar = :avatar where u.id = :userId")
    void setAvatarForUser(@Param("avatar") Avatar avatar, @Param("userId") UUID userId);

    @Query(value = "SELECT avatar_id FROM users WHERE id = :userId", nativeQuery = true)
    UUID findAvatarIdByUserId(@Param("userId") UUID userId);

    @Query(value = "SELECT u.* FROM users u JOIN passes p on u.id = p.user_id WHERE p.id = :passId", nativeQuery = true)
    User findByPassId(@Param("passId") UUID passId);

}
