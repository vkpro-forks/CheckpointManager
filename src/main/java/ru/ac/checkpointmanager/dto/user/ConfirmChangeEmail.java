package ru.ac.checkpointmanager.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmChangeEmail {

    private String previousEmail;
    private String newEmail;
    private String verifiedToken;
}
