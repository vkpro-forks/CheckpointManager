package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.model.TemporaryUser;

public interface TemporaryUserService {
    TemporaryUser findByVerifiedToken(String verifiedToken);

    TemporaryUser create(TemporaryUser temporaryUser);

    void delete(TemporaryUser temporaryUser);
}
