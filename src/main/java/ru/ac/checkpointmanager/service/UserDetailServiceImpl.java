package ru.ac.checkpointmanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ac.checkpointmanager.dto.UserDetailsDTO;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.repository.UserRepository;

import java.util.Optional;

import static ru.ac.checkpointmanager.utils.Mapper.toUserDetailsDTO;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional  /* метод преобразует юзера в вид, который понимает спринг */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(
                String.format("User with email - '%s', not found  ", username)));

        UserDetailsDTO userDetailsDTO = toUserDetailsDTO(user);

        return new org.springframework.security.core.userdetails.User(
                userDetailsDTO.getUsername(),
                userDetailsDTO.getPassword(),
                userDetailsDTO.getAuthorities());
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
