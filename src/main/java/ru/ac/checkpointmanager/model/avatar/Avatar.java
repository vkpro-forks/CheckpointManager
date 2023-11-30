package ru.ac.checkpointmanager.model.avatar;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    private String filePath; // Путь к файлу большого изображения

    private Long fileSize;

    private byte[] preview; // Данные маленького превью изображения

    @OneToOne(mappedBy = "avatar", fetch = FetchType.LAZY, optional = false)
    private User user;
}
