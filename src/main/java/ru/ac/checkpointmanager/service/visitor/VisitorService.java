package ru.ac.checkpointmanager.service.visitor;

import ru.ac.checkpointmanager.model.Visitor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VisitorService {

    Visitor addVisitor(Visitor visitor);

    Visitor getVisitor(UUID uuid);

    Visitor updateVisitor(UUID uuid, Visitor visitor);

    void deleteVisitor(UUID uuid);

    List<Visitor> findByNamePart(String name);

    List<Visitor> findByPhonePart(String phone);

    Optional<Visitor> findByPassId(UUID passId);

    List<Visitor> findByUserId(UUID userId);

    boolean existsById(UUID id);
}
