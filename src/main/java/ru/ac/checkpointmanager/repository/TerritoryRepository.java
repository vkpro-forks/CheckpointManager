package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ac.checkpointmanager.model.Territory;

import java.util.List;

@Repository
public interface TerritoryRepository extends JpaRepository<Territory, Integer> {
    List<Territory> findTerritoriesByNameContainingIgnoreCase(String name);
}
