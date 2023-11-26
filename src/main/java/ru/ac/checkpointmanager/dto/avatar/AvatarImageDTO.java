package ru.ac.checkpointmanager.dto.avatar;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Dto contains all the data prepared for transferring to user
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AvatarImageDTO {

    private UUID avatarId;

    private String mediaType;

    private byte[] imageData;

    private byte[] previewData;

    private Long fileSize;

}
