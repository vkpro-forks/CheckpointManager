package ru.ac.checkpointmanager.security.authfacade;

import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.model.Phone;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.service.phone.PhoneService;

import java.util.UUID;

@Component("phoneAuthFacade")
public final class PhoneAuthFacade implements AuthFacade {

    private final PhoneService phoneService;

    private PhoneAuthFacade(PhoneService phoneService) {
        this.phoneService = phoneService;
    }

    @Override
    public boolean isIdMatch(UUID phoneId) {
        User user = getCurrentUser();
        Phone phone = phoneService.findPhoneById(phoneId);
        return phone.getUser().getId().equals(user.getId());
    }
}
