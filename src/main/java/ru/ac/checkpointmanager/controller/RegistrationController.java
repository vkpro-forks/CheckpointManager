package ru.ac.checkpointmanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.service.UserService;

@RestController
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;


}
