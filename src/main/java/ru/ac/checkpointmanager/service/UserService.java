package ru.ac.checkpointmanager.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.UserAuthDTO;
import ru.ac.checkpointmanager.dto.UserDTO;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService {

    UserAuthDTO createUser(UserAuthDTO userAuthDTO);

    UserDTO findById(UUID id);

    List<TerritoryDTO> findTerritoriesByUserId(UUID userId);

    Collection<UserDTO> findByName(String name);

    UserAuthDTO updateUser(UserAuthDTO userAuthDTO);

    UserDTO updateBlockStatus(UUID id, Boolean isBlocked);

    void blockById(UUID id);

    void unblockById(UUID id);

    void deleteUser(UUID id);

    Collection<UserDTO> getAll();

    Collection<String> findUsersPhoneNumbers(UUID userId);
}
