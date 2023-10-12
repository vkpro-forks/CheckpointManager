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
    @Column(name = "avatar_holder")
    private UUID avatarHolder;

    @Column(name = "media_type")
    private String mediaType;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "preview")
    private byte[] preview;
}
