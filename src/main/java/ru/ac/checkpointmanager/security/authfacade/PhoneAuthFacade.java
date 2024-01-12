package ru.ac.checkpointmanager.security.authfacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.model.Phone;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.service.phone.PhoneService;

import java.util.UUID;

@Component("phoneAuthFacade")
@RequiredArgsConstructor
@Slf4j
public class PhoneAuthFacade implements AuthFacade {

    private final PhoneService phoneService;

    @Override
    public boolean isIdMatch(UUID phoneId) {
        User user = getCurrentUser();
        Phone phone = phoneService.findPhoneById(phoneId);
        return phone.getUser().equals(user);
    }
}
