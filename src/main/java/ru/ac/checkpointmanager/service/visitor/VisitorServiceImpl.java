package ru.ac.checkpointmanager.service.visitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

    private final VisitorRepository repository;
    private final UserService userService;

    @Override
    public Visitor addVisitor(Visitor visitor) {
        if (visitor == null) {
            log.warn("Attempt to add null Visitor");
            throw new IllegalArgumentException("Visitor cannot be null");
        }
        log.info("Adding new Visitor: {}", visitor);
        return repository.save(visitor);
    }

    @Override
    public Visitor getVisitor(UUID uuid) {
        if (uuid == null) {
            log.warn("Attempt to get Visitor with null UUID");
            throw new IllegalArgumentException("UUID cannot be null");
        }
        Optional<Visitor> visitorOptional = repository.findById(uuid);
        return visitorOptional.orElseThrow(() -> {
            log.warn("Visitor not found for UUID: {}", uuid);
            return new VisitorNotFoundException("Visitor not found");
        });
    }

    @Override
    public Visitor updateVisitor(UUID uuid, Visitor visitor) {
        if (uuid == null || visitor == null) {
            log.warn("Attempt to update Visitor with null UUID or null Visitor");
            throw new IllegalArgumentException("UUID or Visitor cannot be null");
        }
        Visitor existVisitor = getVisitor(uuid);
        log.info("Updating Visitor with UUID: {}, new data: {}", uuid, visitor);
        existVisitor.setName(visitor.getName());
        existVisitor.setPhone(visitor.getPhone());
        existVisitor.setNote(visitor.getNote());
        return repository.save(existVisitor);
    }

    @Override
    public void deleteVisitor(UUID uuid) {
        if (uuid == null) {
            log.warn("Attempt to delete Visitor with null UUID");
            throw new IllegalArgumentException("UUID cannot be null");
        }
        Visitor existVisitor = getVisitor(uuid);
        log.info("Deleting Visitor with UUID: {}", uuid);
        repository.delete(existVisitor);
    }

    @Override
    public List<Visitor> findByNamePart(String name) {
        if (name == null || name.isEmpty()) {
            log.warn("Attempt to find Visitor by null or empty name");
            throw new IllegalArgumentException("Name part cannot be null or empty");
        }
        log.info("Searching for Visitors with name containing: {}", name);
        return repository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<Visitor> findByPhonePart(String phone) {
        if (phone == null || phone.isEmpty()) {
            log.warn("Attempt to find Visitor by null or empty phone");
            throw new IllegalArgumentException("Phone part cannot be null or empty");
        }
        log.info("Searching for Visitors with phone containing: {}", phone);
        return repository.findByPhoneContaining(phone);
    }

    @Override
    public Optional<Visitor> findByPassId(UUID passId) {
        if (passId == null) {
            log.warn("Attempt to find Visitor by null passId");
            throw new IllegalArgumentException("Pass ID cannot be null");
        }
        log.info("Searching for Visitor with Pass ID: {}", passId);
        return repository.findVisitorByPasses_Id(passId);
    }

    @Override
    public List<Visitor> findByUserId(UUID userId) {
        log.debug("Method {} [UUID - {}]", MethodLog.getMethodName(), userId);
        userService.findById(userId);

        List<Visitor> foundVisitors = repository.findVisitorsByUserId(userId);
        log.debug("Find {} Visitors for user [UUID - {}]", foundVisitors.size(), userId);
        return foundVisitors;
    }
}
