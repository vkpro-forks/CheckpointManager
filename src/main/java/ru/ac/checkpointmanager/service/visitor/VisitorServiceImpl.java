package ru.ac.checkpointmanager.service.visitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.exception.ExceptionUtils;
import ru.ac.checkpointmanager.exception.VisitorNotFoundException;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.repository.VisitorRepository;
import ru.ac.checkpointmanager.service.user.UserService;
import ru.ac.checkpointmanager.utils.MethodLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitorServiceImpl implements VisitorService {

    private final VisitorRepository visitorRepository;
    private final UserService userService;

    @Override
    public Visitor addVisitor(Visitor visitor) {
        log.info("Adding new Visitor: {}", visitor);
        return visitorRepository.save(visitor);
    }

    @Override
    public Visitor getVisitor(UUID visitorId) {
        Optional<Visitor> visitorOptional = visitorRepository.findById(visitorId);
        return visitorOptional.orElseThrow(() -> {
            log.warn(ExceptionUtils.VISITOR_NOT_FOUND.formatted(visitorId));
            return new VisitorNotFoundException(ExceptionUtils.VISITOR_NOT_FOUND.formatted(visitorId));
        });
    }

    @Override
    public Visitor updateVisitor(UUID uuid, Visitor visitor) {
        Visitor existVisitor = getVisitor(uuid);
        log.info("Updating Visitor with UUID: {}, new data: {}", uuid, visitor);
        existVisitor.setName(visitor.getName());
        existVisitor.setPhone(visitor.getPhone());
        existVisitor.setNote(visitor.getNote());
        return visitorRepository.save(existVisitor);
    }

    @Override
    public void deleteVisitor(UUID visitorId) {
        if (!visitorRepository.existsById(visitorId)) {
            log.warn(ExceptionUtils.VISITOR_NOT_FOUND.formatted(visitorId));
            throw new VisitorNotFoundException(ExceptionUtils.VISITOR_NOT_FOUND.formatted(visitorId));
        }
        visitorRepository.deleteById(visitorId);
        log.info("Visitor with id: {} deleted", visitorId);
    }

    @Override
    public List<Visitor> findByNamePart(String name) {
        log.info("Searching for Visitors with name containing: {}", name);
        return visitorRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Visitor> findByPhonePart(String phone) {
        log.info("Searching for Visitors with phone containing: {}", phone);
        return visitorRepository.findByPhoneContaining(phone);
    }

    @Override
    public Optional<Visitor> findByPassId(UUID passId) {
        log.info("Searching for Visitor with Pass ID: {}", passId);
        return visitorRepository.findVisitorByPasses_Id(passId);
    }

    @Override
    public List<Visitor> findByUserId(UUID userId) {
        log.debug("Method {} [UUID - {}]", MethodLog.getMethodName(), userId);
        userService.findById(userId);

        List<Visitor> foundVisitors = visitorRepository.findVisitorsByUserId(userId);
        log.debug("Find {} Visitors for user [UUID - {}]", foundVisitors.size(), userId);
        return foundVisitors;
    }

}
