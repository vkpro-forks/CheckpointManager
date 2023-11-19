package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ac.checkpointmanager.model.Visitor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, UUID> {

    List<Visitor> findByNameContainingIgnoreCase(String name);
    List<Visitor> findByPhoneContaining(String phone);
    Optional<Visitor> findVisitorByPasses_Id(UUID passId);
    Optional<Visitor> findByPhone(String name);

    @Query(value = "SELECT v.* FROM visitors v JOIN passes p ON v.id = p.visitor_id WHERE p.user_id = :userId"
            , nativeQuery = true)
    List<Visitor> findVisitorsByUserId(@Param("userId") UUID userId);

}
