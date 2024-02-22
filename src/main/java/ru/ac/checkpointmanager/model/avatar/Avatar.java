package ru.ac.checkpointmanager.model.avatar;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;

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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String mediaType;

    private String filePath;

    private Long fileSize;

    private byte[] preview;

    @OneToOne(mappedBy = "avatar", fetch = FetchType.LAZY, optional = false)
    private User user;

    @OneToOne(mappedBy = "avatar", fetch = FetchType.LAZY, optional = false)
    private Territory territory;
}
