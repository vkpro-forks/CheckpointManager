package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.model.TemporaryUser;
import ru.ac.checkpointmanager.repository.TemporaryUserRepository;

@Service
@RequiredArgsConstructor
public class TemporaryUserServiceImpl implements TemporaryUserService {

    private final TemporaryUserRepository repository;

    @Override
    public TemporaryUser findByVerifiedToken(String verifiedToken) {
        return repository.findByVerifiedToken(verifiedToken);
    }

    @Override
    public TemporaryUser create(TemporaryUser temporaryUser) {
        return repository.save(temporaryUser);
    }

    @Override
    public void delete(TemporaryUser temporaryUser) {
        repository.delete(temporaryUser);
    }

//    @Override
//    public void deleteByTempToken(String verifiedToken) {
//        TemporaryUser foundUser = findByVerifiedToken(verifiedToken);
//        if (foundUser == null) {
//            throw new UserNotFoundException("Wrong verifying token");
//        }
//        delete(foundUser);
//    }
}
