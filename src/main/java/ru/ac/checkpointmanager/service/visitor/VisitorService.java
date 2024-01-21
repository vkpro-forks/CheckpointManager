package ru.ac.checkpointmanager.service.visitor;

import ru.ac.checkpointmanager.dto.VisitorDTO;

import java.util.List;
import java.util.UUID;

public interface VisitorService {

    VisitorDTO addVisitor(VisitorDTO visitorDTO);

    VisitorDTO getVisitor(UUID visitorId);

    VisitorDTO updateVisitor(UUID visitorId, VisitorDTO visitorDTO);

    void deleteVisitor(UUID visitorId);

    List<VisitorDTO> findByNamePart(String name);

    List<VisitorDTO> findByPhonePart(String phone);

    VisitorDTO findByPassId(UUID passId);

    List<VisitorDTO> findByUserId(UUID userId);

}
