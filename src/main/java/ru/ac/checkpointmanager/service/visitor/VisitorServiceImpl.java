package ru.ac.checkpointmanager.service.visitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.VisitorNotFoundException;
import ru.ac.checkpointmanager.mapper.VisitorMapper;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.repository.VisitorRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class VisitorServiceImpl implements VisitorService {

    private final VisitorRepository visitorRepository;
    private final VisitorMapper visitorMapper;

    @Override
    @Transactional
    public VisitorDTO addVisitor(VisitorDTO visitorDTO) {
        Visitor visitor = visitorMapper.toVisitor(visitorDTO);
        Visitor savedVisitor = visitorRepository.save(visitor);
        log.info("New Visitor added id: {}", visitorDTO);
        return visitorMapper.toVisitorDTO(savedVisitor);
    }

    @Override
    public VisitorDTO getVisitor(UUID visitorId) {
        Visitor visitor = visitorRepository.findById(visitorId).orElseThrow(() -> {
            log.warn(ExceptionUtils.VISITOR_NOT_FOUND.formatted(visitorId));
            return new VisitorNotFoundException(ExceptionUtils.VISITOR_NOT_FOUND.formatted(visitorId));
        });
        log.debug("Visitor with id {} retrieved from DB", visitorId);
        return visitorMapper.toVisitorDTO(visitor);
    }

    @Override
    @Transactional
    public VisitorDTO updateVisitor(UUID visitorId, VisitorDTO visitorDTO) {
        Visitor existVisitor = visitorRepository.findById(visitorId).orElseThrow(() -> {
            log.warn(ExceptionUtils.VISITOR_NOT_FOUND.formatted(visitorId));
            return new VisitorNotFoundException(ExceptionUtils.VISITOR_NOT_FOUND.formatted(visitorId));
        });
        existVisitor.setName(visitorDTO.getName());
        existVisitor.setPhone(visitorDTO.getPhone());
        existVisitor.setNote(visitorDTO.getNote());
        Visitor updatedVisitor = visitorRepository.save(existVisitor);
        log.info("Visitor with id: {} updated", visitorId);
        return visitorMapper.toVisitorDTO(updatedVisitor);
    }

    @Override
    @Transactional
    public void deleteVisitor(UUID visitorId) {
        if (!visitorRepository.existsById(visitorId)) {
            log.warn(ExceptionUtils.VISITOR_NOT_FOUND.formatted(visitorId));
            throw new VisitorNotFoundException(ExceptionUtils.VISITOR_NOT_FOUND.formatted(visitorId));
        }
        visitorRepository.deleteById(visitorId);
        log.info("Visitor with id: {} deleted", visitorId);
    }

    @Override
    public List<VisitorDTO> findByNamePart(String name) {
        log.debug("Searching for Visitors with name containing: {}", name);
        List<Visitor> visitors = visitorRepository.findByNameContainingIgnoreCase(name);
        return visitorMapper.toVisitorDTOS(visitors);
    }

    @Override
    public List<VisitorDTO> findByPhonePart(String phone) {
        log.debug("Searching for Visitors with phone containing: {}", phone);
        List<Visitor> visitors = visitorRepository.findByPhoneContaining(phone);
        return visitorMapper.toVisitorDTOS(visitors);
    }

    @Override
    public VisitorDTO findByPassId(UUID passId) {
        Visitor visitor = visitorRepository.findVisitorByPasses_Id(passId).orElseThrow(() -> {
            log.warn(ExceptionUtils.VISITOR_BY_PASS_NOT_FOUND.formatted(passId));
            return new VisitorNotFoundException(ExceptionUtils.VISITOR_BY_PASS_NOT_FOUND.formatted(passId));
        });
        log.info("Visitor with [pass id: {}] retrieved from DB", passId);
        return visitorMapper.toVisitorDTO(visitor);
    }

    @Override
    public List<VisitorDTO> findByUserId(UUID userId) {
        List<Visitor> foundVisitors = visitorRepository.findVisitorsByUserId(userId);
        log.debug("Find {} Visitors for user [UUID - {}]", foundVisitors.size(), userId);
        return visitorMapper.toVisitorDTOS(foundVisitors);
    }

}
