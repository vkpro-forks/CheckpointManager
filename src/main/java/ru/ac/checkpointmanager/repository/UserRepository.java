package ru.ac.checkpointmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ac.checkpointmanager.model.User;

import java.util.Collection;

public interface UserRepository extends JpaRepository<User, Long> {
    Collection<User> findUserByFullNameContainingIgnoreCase(String name);
}
