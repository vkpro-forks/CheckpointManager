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

    //select v1_0.id,v1_0.full_name,v1_0.note,v1_0.visitor_phone from visitors v1_0 left join passes p1_0 on v1_0.id=p1_0.visitor_id and p1_0.dtype='WALK' where p1_0.id=?

    @Query(value = "SELECT v.* FROM visitors v JOIN passes p ON v.id = p.visitor_id WHERE p.user_id = :userId"
            , nativeQuery = true)
    List<Visitor> findVisitorsByUserId(@Param("userId") UUID userId);

    @Query(value = "SELECT EXISTS (" +
            "SELECT * FROM passes p " +
            "JOIN visitors v ON p.visitor_id = v.id " +
            "WHERE v.id = :visitorId AND p.user_id = :userId)"
            , nativeQuery = true)
    boolean checkUserVisitorRelation(@Param("userId") UUID userId, @Param("visitorId") UUID visitorId);
}
