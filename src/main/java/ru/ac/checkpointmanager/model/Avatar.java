package ru.ac.checkpointmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Avatar entity class. There is no fixed list of entities
 * that can have avatar.
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "avatars")
public class Avatar {

    /**
     * UUID of Entity related to the avatar
     */
    @Id
    private UUID avatarHolder;

    private String mediaType;

    private String filePath;

    private Long fileSize;

    private byte[] preview;
}
