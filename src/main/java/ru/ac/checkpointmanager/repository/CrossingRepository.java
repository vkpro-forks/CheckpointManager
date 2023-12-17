package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ac.checkpointmanager.model.Crossing;

import java.util.List;
import java.util.UUID;

@Repository
public interface CrossingRepository extends JpaRepository<Crossing, UUID> {

    List<Crossing> findCrossingsByPassId(UUID passId);
}
